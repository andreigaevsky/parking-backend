package com.parking.spring.dao.entity;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @Generated
    private Long id;
}
