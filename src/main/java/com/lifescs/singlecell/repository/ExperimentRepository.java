package com.lifescs.singlecell.repository;

import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.Experiment;

public interface ExperimentRepository extends CrudRepository<Experiment, String> {

}
