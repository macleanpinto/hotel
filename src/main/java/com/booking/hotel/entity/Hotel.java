package com.booking.hotel.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Table
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {
    @Id
    private int id;
    private String name;
}
