package com.lifescs.singlecell.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.GeneExpressionList;

public interface GeneExpressionListRepository extends CrudRepository<GeneExpressionList, ObjectId> {

}
