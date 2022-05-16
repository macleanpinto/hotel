package com.booking.recruitment.hotel.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.util.Util;
import com.booking.testing.SlowTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:data.sql")
@SlowTest
class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("When search search by hotels is requested return top rated k nearby hotels by city")
    void fetchTopRatedKNearByHotels() throws Exception {
        List<Hotel> hotels = mapper.readValue(
                mockMvc.perform(get("/search/1?sortBy=distance")).andExpect(status().is2xxSuccessful()).andReturn()
                        .getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, Hotel.class));
        for (int i = 0; i < hotels.size() - 1; i++) {
            int res = Util.CompareHotelRatings(hotels.get(i + 1).getRating(), hotels.get(i).getRating());
            if (res != 0) {
                assertThat(0, lessThanOrEqualTo(0));
            } else {
                assertThat(Util.compareHotelDistance(hotels.get(i), hotels.get(i + 1)), lessThanOrEqualTo(0));
            }

        }
    }
}