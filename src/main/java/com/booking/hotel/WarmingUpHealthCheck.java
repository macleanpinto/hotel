package com.booking.hotel;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.RateLimiter;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Warming Server Health-Check
 */
public class WarmingUpHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(WarmingUpHealthCheck.class);

    private static final String BASE_URL = "http://localhost:8085";

    private static final AsyncHttpClient AHC_CLIENT = new DefaultAsyncHttpClient(
            new DefaultAsyncHttpClientConfig.Builder()
                    .setUserAgent("mss.warmup (ahc-2.x)")
                    .setConnectionTtl(-1)
                    .setMaxConnections(100)
                    .setRequestTimeout(500)
                    .setConnectTimeout(1000)
                    .setKeepAlive(true)
                    .build());

    @VisibleForTesting
    final AtomicBoolean isWarmedUp = new AtomicBoolean(false);

    private final Configuration warmUpConfig;
    private final Collection<Double> responseTimes;
    private final RateLimiter rateLimiter;

    public WarmingUpHealthCheck() {
        this(new Configuration(20000,
                Duration.ofMillis(2),
                Duration.ofSeconds(10),
                Duration.ofMinutes(5), 0.99D));
    }

    @VisibleForTesting
    WarmingUpHealthCheck(Configuration configuration) {
        this.warmUpConfig = configuration;

        this.responseTimes = Collections.synchronizedCollection(
                new CircularFifoQueue<>(configuration.rateLimit)); // only hold rate limit data

        this.rateLimiter = RateLimiter.create(configuration.rateLimit);
    }

    /**
     * This method needs to be run prior to registration of this healthcheck
     */
    public void run() {
        LOGGER.info("Running WarmUp sequence with settings: {}", warmUpConfig);
        CompletableFuture.runAsync(this::warmUp);
    }

    private void warmUp() {
        ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean timedOut = new AtomicBoolean(false);
        ScheduledFuture timeoutFuture = timeoutScheduler.schedule(() -> {
            LOGGER.error("warmer timed out before expected threshold");
            timedOut.set(true);
            isWarmedUp.set(false);
        }, warmUpConfig.timeout.getSeconds(), TimeUnit.SECONDS);

        doWarmUp(timedOut).thenApply(result -> {
            timeoutFuture.cancel(true);
            timeoutScheduler.shutdownNow();
            isWarmedUp.set(result);
            LOGGER.info("warmup complete with result {}, shutdown timer", result);
            return result;
        });
    }

    private CompletableFuture<Boolean> doWarmUp(AtomicBoolean timedOut) {
        return CompletableFuture.supplyAsync(() -> {
            AtomicBoolean rampUpOver = new AtomicBoolean();
            ScheduledExecutorService rampUpScheduler = Executors.newSingleThreadScheduledExecutor();
            rampUpScheduler.schedule(() -> rampUpOver.set(true), warmUpConfig.rampUpDuration.getSeconds(),
                    TimeUnit.SECONDS);

            while (true) {
                if (timedOut.get()) { // if timed out then bad
                    return false;
                }
                
                if (responseTimes.size() >= warmUpConfig.rateLimit && isPercentileAchieved(responseTimes.toArray())) {
                    LOGGER.info("RPS achieved");
                    return true;
                }
                rateLimiter.acquire();
                doRequest().handle((response, th) -> {
                    if (!rampUpOver.get()) {
                        LOGGER.debug("discarding results, under ramp up");
                        return null;
                    }
                    if (th == null) {
                        if (response == 0d) {
                            LOGGER.debug("discarding zero response results. something wrong with request?");
                        } else {
                            responseTimes.add((double) response);
                            LOGGER.debug("adding response {}, total response size {}", response, responseTimes.size());
                        }
                    } else {
                        LOGGER.debug("error running task, exception message {}", th.getLocalizedMessage());
                    }
                    return null;
                });
            }
        });
    }

    private CompletableFuture<Long> doRequest() {
        return CompletableFuture.supplyAsync(this::prepareRequest)
                .thenCompose(request -> AHC_CLIENT.executeRequest(request).toCompletableFuture()
                        .handle((response, ex) -> {
                            long requestTime = Long.parseLong(request.getHeaders().get("X-Request-Time-Ms"));
                            return System.currentTimeMillis() - requestTime;
                        }));
    }

    private Request prepareRequest() {
        return new RequestBuilder("GET")
                .setUri(Uri.create(getBaseUrl() + "/hotels/"))
                .setHeader("Content-Type", "application/json")
                .setHeader("X-Request-Time-Ms", System.currentTimeMillis())
                .build();
    }

    private String getBaseUrl() {
        return BASE_URL;
    }

    private boolean isPercentileAchieved(final Object[] responseTimes) {
        double evaluate = evaluatePercentile(responseTimes);
        LOGGER.info("WarmUp Percentile Evaluated to be: {} ms. WarmUp target: {} ms", evaluate,
                warmUpConfig.successThreshold.toMillis());
        return evaluate <= warmUpConfig.successThreshold.toMillis();
    }

    private double evaluatePercentile(Object[] responseTimes) {
        final Percentile percentile = new Percentile();
        double[] values = Arrays.stream(responseTimes).mapToDouble(s -> (Double) s).toArray();
        return percentile.evaluate(values, warmUpConfig.percentile);
    }

    @Override
    public String toString() {
        return "ServerUpHealthCheck";
    }

    static class Configuration {
        public Configuration(int rateLimit, Duration successThreshold, Duration rampUpDuration, Duration timeout,
                double percentile) {
            this.rateLimit = rateLimit;
            this.successThreshold = successThreshold;
            this.rampUpDuration = rampUpDuration;
            this.timeout = timeout;
            this.percentile = percentile;
        }

        /**
         * The maximum number of QPS to send
         */
        public final int rateLimit;

        /**
         * success threshold percentile to determine successful warmup
         */
        public final Duration successThreshold;

        /**
         * ramp-up up duration. This duration is used to slowly ramp up request rate
         * before threshold evaluation starts.
         * Results are not evaluated till ramp up time is achieved.
         */
        public final Duration rampUpDuration;

        /**
         * The maximum number of seconds to wait for the warm-up phase to complete. This
         * include ALL warmup, including
         * the {@linkplain Configuration#rampUpDuration}.
         */
        public final Duration timeout;

        /**
         * Percentile of calculated response times to check against target
         */
        public final Double percentile;

        @Override
        public String toString() {
            return "Configuration{" +
                    "rateLimit=" + rateLimit +
                    ", successThreshold=" + successThreshold +
                    ", rampUpDuration=" + rampUpDuration +
                    ", timeout=" + timeout +
                    ", percentile=" + percentile +
                    '}';
        }
    }
}