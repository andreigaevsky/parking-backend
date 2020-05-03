package com.parking.spring.impl;

import com.parking.spring.converter.ConverterService;
import com.parking.spring.dao.entity.Parking;
import com.parking.spring.dao.repos.ParkingRepo;
import com.parking.spring.dto.ParkingCreateDto;
import com.parking.spring.dto.ParkingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingService {
    @Autowired
    private ParkingRepo parkingRepo;

    @Autowired
    private ConverterService converterService;

    public List<ParkingDto> getAllParkingInfo() {
        return  converterService.toDtoList(parkingRepo.findAll(), ParkingDto.class);
    }

    public ParkingDto createNew(ParkingCreateDto newParking) throws IllegalArgumentException{
        Parking parking;
        parking = converterService.toEntity(newParking, Parking.class).orElseThrow(IllegalArgumentException::new);
        parking.setAllSlotsCount(0);
        parking.setFreeSlotsCount(0);
        return converterService.toDto(parkingRepo.save(parking),ParkingDto.class).orElse(null);
    }

}
