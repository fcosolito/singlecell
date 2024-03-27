package com.lifescs.singlecell.dao.plot;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.api.ClusterTreeDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClusterTreeDao {
  private MongoTemplate mongoTemplate;

  public List<ClusterTreeDto> getDtos(Experiment experiment){
    MatchOperation matchExperiment = Aggregation.match(Criteria.where("experiment.$id").is(experiment.getId()));
    UnwindOperation unwindCellClusters = Aggregation.unwind("cellClusters");
    GroupOperation groupByClusterId = Aggregation.group("cellClusters.cluster")
    .first("cellClusters.resolution").as("resolution")
    .count().as("cellCount");
    LookupOperation lookupCluster = Aggregation.lookup(
      "cluster",
      "_id.$id",
      "_id",
      "clusterInfo"
    );
    UnwindOperation unwindClusterInfo = Aggregation.unwind("clusterInfo");
    GroupOperation groupByClusterName = Aggregation.group("resolution", "clusterInfo.name")
    .sum("cellCount").as("cellCount");
    LookupOperation lookupResolution = Aggregation.lookup(
      "resolution",
      "_id.resolution.$id",
      "_id",
      "resolutionInfo"
    );
    UnwindOperation unwindResolutionInfo = Aggregation.unwind("resolutionInfo");
    ProjectionOperation project = Aggregation.project("cellCount")
    .and("resolutionInfo.name").as("resolution")
    .and("_id.name").as("cluster")
    .andExclude("_id");

    Aggregation aggregation = Aggregation.newAggregation(
      matchExperiment,
      unwindCellClusters,
      groupByClusterId,
      lookupCluster,
      unwindClusterInfo,
      groupByClusterName,
      lookupResolution,
      unwindResolutionInfo,
      project
    );

    return mongoTemplate.aggregate(aggregation, "cell", ClusterTreeDto.class).getMappedResults();
  }
}
