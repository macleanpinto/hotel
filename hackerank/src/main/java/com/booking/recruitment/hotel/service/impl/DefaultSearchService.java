package com.booking.recruitment.hotel.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.service.HotelService;
import com.booking.recruitment.hotel.service.SearchService;
import com.booking.recruitment.hotel.util.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultSearchService implements SearchService {

    private final HotelService hotelService;

    @Autowired
    DefaultSearchService(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * Method to find top-rated k number of hotels closer to a city center
     *
     * @param cityId
     * @param pageSize Number of hotels closer to city required
     * @return list of hotels closer to a city center
     */
    @Override
    public List<Hotel> searchTopRatedKNearByHotels(Long cityId, Integer pageSize) {
        List<Hotel> hotels = hotelService.getHotelsByCity(cityId);
        if (hotels.isEmpty())
            return new ArrayList<>();

        PriorityQueue<Hotel> pq = new PriorityQueue<Hotel>(new Comparator<Hotel>() {
            public int compare(Hotel h1, Hotel h2) {
                int res = Util.CompareHotelRatings(h2.getRating(), h1.getRating());
                if (res == 0) {
                    return Util.compareHotelDistance(h1, h2);
                }
                return res;

            }
        });

        for (Hotel hotel : hotels) {
            pq.add(hotel);
            if (pq.size() > pageSize) {
                pq.poll();
            }
        }

        List<Hotel> nearbyHotels = new LinkedList<>();
        while (!pq.isEmpty()) {
            nearbyHotels.add(pq.poll());
        }
        return nearbyHotels;
    }
}