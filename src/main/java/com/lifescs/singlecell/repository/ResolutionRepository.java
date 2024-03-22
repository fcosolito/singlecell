package com.lifescs.singlecell.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.Resolution;

public interface ResolutionRepository extends CrudRepository<Resolution, String> {

    Optional<Resolution> findByName(String name);

}
