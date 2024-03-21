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
    LookupOperation lookupCluster = Aggregation.lookup(
      "cluster",
      "cluster.$id",
      "_id",
      "clusterInfo"
    );

    MatchOperation matchResolution = Aggregation.match(Criteria.where("clusterInfo.resolution.$id")
      .is(new ObjectId(resolution.getId())));

    // TODO
    ProjectionOperation projectDtos = Aggregation.project();

    Aggregation aggregation = Aggregation.newAggregation(
      lookupCluster,
      matchResolution,
      projectDtos
    );
    List<HeatmapDto> result = mongoTemplate.aggregate(aggregation, "heatmapCluster", HeatmapDto.class).getMappedResults();
    return result;
  }

}
