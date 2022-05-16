package com.booking.recruitment.hotel.service;

import java.util.List;

import com.booking.recruitment.hotel.model.Hotel;

import org.springframework.stereotype.Service;

@Service
public interface SearchService {
    List<Hotel> searchTopRatedKNearByHotels(Long cityId, Integer pageSize);
}