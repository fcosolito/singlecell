package com.lifescs.singlecell.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.ListCrudRepository;

import com.lifescs.singlecell.model.HeatmapCluster;

public interface HeatmapClusterRepository extends ListCrudRepository<HeatmapCluster, ObjectId> {

}
