package com.lifescs.singlecell.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.Sample;

public interface SampleRepository extends CrudRepository<Sample, String> {
    Optional<Sample> findByName(String name);

}
