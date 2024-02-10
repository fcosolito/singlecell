package com.lifescs.singlecell.repository;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.GeneExpression;

public interface GeneExpressionRepository extends CrudRepository<GeneExpression, String> {

}
