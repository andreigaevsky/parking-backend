package com.parking.spring.converter;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ConverterService {

    @Autowired
    private DozerBeanMapper dozerMapper;

    public <E, D> Optional<D> toDto(E entity, Class<D> dtoClass) {
        return to(entity, dtoClass);
    }

    public <E, D> List<D> toDtoList(List<E> entities, Class<D> dtoClass) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<D> resultList = new ArrayList<>();

        for (E entity : entities) {
            if (entity == null) {
                continue;
            }

            toDto(entity, dtoClass).ifPresent(resultList::add);
        }

        return resultList;
    }

    public <D, E> Optional<E> toEntity(D dto, Class<E> entityClass) {
        return to(dto, entityClass);
    }

    private <S, T> Optional<T> to(S source, Class<T> targetClass) {
        if (source == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(dozerMapper.map(source, targetClass));
    }

}
