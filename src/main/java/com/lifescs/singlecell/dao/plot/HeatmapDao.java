package com.lifescs.singlecell.dao.plot;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class HeatmapDao {
  private MongoTemplate mongoTemplate;

  public List<HeatmapDto> getHeatmapDtos(Resolution r) {
    MatchOperation matchCluster = Aggregation.match(Criteria.where("resolution").is(r.getId()));

    LookupOperation lookupHeatmapCluster = LookupOperation.newLookup()
        .from("heatmapCluster")
        .localField("heatmapClusterId")
        .foreignField("_id")
        .as("heatmapInfo");

    ProjectionOperation project = Aggregation.project("name")
        .and("heatmapInfo.buckets").as("buckets")
        .and("heatmapInfo.topMarkers").as("markers")
        .and("heatmapInfo.expressions").as("expressions");

    UnwindOperation unwindExpressions = Aggregation.unwind("$expressions");
    UnwindOperation unwindBuckets = Aggregation.unwind("$buckets");
    UnwindOperation unwindMarkers = Aggregation.unwind("$markers");

    Aggregation aggregation = Aggregation.newAggregation(
        matchCluster,
        lookupHeatmapCluster,
        project,
        unwindMarkers,
        unwindExpressions,
        unwindBuckets);

    List<HeatmapDto> result = mongoTemplate
        .aggregate(aggregation, "cluster", HeatmapDto.class)
        .getMappedResults();

    if (result.isEmpty())
      throw new NoObjectFoundException("No heatmap clusters found for resolution: " + r.getId());
    else
      return result;
  }

  public List<HeatmapDto> getHeatmapDtos2(Resolution resolution){
    MatchOperation matchClusters = Aggregation.match(Criteria.where("resolution.$id")
      .is(new ObjectId(resolution.getId())));

    LookupOperation lookupHeatmapClusters = Aggregation.lookup(
      "heatmapCluster",
      "_id",
      "cluster.$id",
      "heatmapInfo"
    );

    UnwindOperation unwindHeatmaps = Aggregation.unwind("heatmapInfo");

    // TODO
    ProjectionOperation projectDtos = Aggregation.project("name")
    .and("heatmapInfo.topMarkers").as("markers")
    .and("heatmapInfo.buckets").as("buckets")
    .and("heatmapInfo.expressions").as("expressions");

    Aggregation aggregation = Aggregation.newAggregation(
      matchClusters,
      lookupHeatmapClusters,
      unwindHeatmaps,
      projectDtos
    );
    List<HeatmapDto> result = mongoTemplate.aggregate(aggregation, "cluster", HeatmapDto.class).getMappedResults();
    if (result.isEmpty())
      throw new NoObjectFoundException("No heatmap clusters found for resolution: " + resolution.getId());
    else
      return result;
  }

}
