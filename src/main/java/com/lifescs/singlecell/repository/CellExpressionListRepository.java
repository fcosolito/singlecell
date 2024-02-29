package com.lifescs.singlecell.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.CellExpressionList;

public interface CellExpressionListRepository extends CrudRepository<CellExpressionList, ObjectId> {

}