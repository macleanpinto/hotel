package com.booking.hotel.service;

import java.util.concurrent.CompletableFuture;

import com.booking.hotel.entity.Hotel;
import com.booking.hotel.repository.HotelRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HotelService {

    @Autowired
    HotelRepository hotelRepository;

    private static Logger logger = LoggerFactory.getLogger(HotelRepository.class);

    public Mono<Hotel> findHotelById(int hotelId) {

        logger.info("In method findHotelById in service class");
        return hotelRepository.findById(hotelId);

    }

    public Flux<Hotel> findHotels() {

        logger.info("In method findHotels in service class");
        return hotelRepository.findAll();

    }
}
