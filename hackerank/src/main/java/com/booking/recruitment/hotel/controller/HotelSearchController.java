package com.booking.recruitment.hotel.controller;

import java.util.List;

import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class HotelSearchController {
    private final SearchService searchService;

    @Autowired
    public HotelSearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/{cityId}")
    public ResponseEntity<List<Hotel>> getTopRatedKNearByHotels(@PathVariable("cityId") Long cityId,
            @RequestParam(name = "sortBy", defaultValue = "distance", required = false) String sortBy,
            @RequestParam(name = "pageSize", defaultValue = "3", required = false) Integer pageSize) {

        if (!sortBy.equals("distance")) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(searchService.searchTopRatedKNearByHotels(cityId, pageSize));
    }

}
