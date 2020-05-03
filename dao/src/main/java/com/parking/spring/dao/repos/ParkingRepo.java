package com.parking.spring.dao.repos;

import com.parking.spring.dao.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingRepo extends JpaRepository<Parking, Long> {

}
