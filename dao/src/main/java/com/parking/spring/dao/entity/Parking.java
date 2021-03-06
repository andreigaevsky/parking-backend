package com.parking.spring.dao.entity;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Parking extends BaseEntity{
    private String address;
    private String lat;
    private String lng;
    private String url;
    @ColumnDefault("false")
    private boolean isConfirmed;
    private int freeSlotsCount;
    private int allSlotsCount;
}
