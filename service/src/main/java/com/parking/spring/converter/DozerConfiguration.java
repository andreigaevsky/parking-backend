package com.parking.spring.converter;

import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DozerConfiguration {

    @Bean
    public DozerBeanMapper dozerMapper() {
        return new DozerBeanMapper();
    }

}
