package com.booking.hotel.repository;

import com.booking.hotel.entity.Hotel;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface HotelRepository extends ReactiveCrudRepository<Hotel, Integer> {
    Mono<Hotel> findById(int id);
}
