package com.booking.hotel.controller;

import com.booking.hotel.entity.Hotel;
import com.booking.hotel.service.HotelService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class HotelController {

    @Autowired
    HotelService hotelService;

    private static Logger logger = LoggerFactory.getLogger(HotelController.class);

    @GetMapping(value = "/hotels", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Hotel> fetchHotels() {

        return hotelService.findHotels().doOnComplete(() -> logger.info("In method fetchHotels in Controller class"));
    }

    @GetMapping(value = "/hotels/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> fetchHotelById(@PathVariable String id) {
        return hotelService.findHotelById(Integer.parseInt(id))
                .map(hotel -> ResponseEntity.status(HttpStatus.OK).body(hotel))
                .cast(ResponseEntity.class)
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hotel found"));
    }

}