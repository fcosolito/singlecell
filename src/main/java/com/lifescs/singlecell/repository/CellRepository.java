package com.lifescs.singlecell.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.lifescs.singlecell.model.Cell;

public interface CellRepository extends ListCrudRepository<Cell, String> {

}
