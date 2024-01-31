package com.lifescs.singlecell.repository;

import org.springframework.data.repository.CrudRepository;

import com.lifescs.singlecell.model.Cluster;

public interface ClusterRepository extends CrudRepository<Cluster, String> {

}
