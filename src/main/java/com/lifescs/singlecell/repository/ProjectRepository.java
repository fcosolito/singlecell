package com.lifescs.singlecell.repository;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.Project;

public interface ProjectRepository extends CrudRepository<Project, String> {

}
