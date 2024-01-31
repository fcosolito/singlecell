package com.lifescs.singlecell.repository;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.User;

public interface UserRepository extends CrudRepository<User, String> {

}
