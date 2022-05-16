package com.booking.recruitment.hotel.controller;

import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/hotel")
public class HotelController {
    private final HotelService hotelService;

    @Autowired
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long hotelId) {
        Optional<Hotel> optionalHotel = hotelService.getHotelById(hotelId);
        return (optionalHotel.isPresent())
                ? ResponseEntity.ok().body(optionalHotel.get())
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<String> deleteHotelById(@PathVariable Long hotelId) {
        Optional<Hotel> optionalHotel = hotelService.getHotelById(hotelId);
        if (!optionalHotel.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        hotelService.deleteById(hotelId);
        return ResponseEntity.ok(String.format("Hotel with id %s is successfully deleted", hotelId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelService.createNewHotel(hotel);
    }
}
