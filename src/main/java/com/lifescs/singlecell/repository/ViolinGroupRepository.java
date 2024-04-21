package com.lifescs.singlecell.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.ViolinGroup;

public interface ViolinGroupRepository extends CrudRepository<ViolinGroup, ObjectId> {

}
