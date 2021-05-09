package com.parking.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParkingDto {
    private Long id;
    private String address;
    private int freeSlotsCount;
    private int allSlotsCount;
    private String lat;
    private String lng;
    private byte[] image;
}
