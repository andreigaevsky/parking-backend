package com.parking.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParkingCreateDto {
    @NotBlank
    private String address;
    @NotNull
    private Double lat;
    @NotNull
    private Double lng;
    @NotBlank
    private String url;
}
