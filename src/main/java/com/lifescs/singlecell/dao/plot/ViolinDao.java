package com.lifescs.singlecell.dao.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.dto.query.ViolinQueryDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ResolutionService;
import com.mongodb.BasicDBObject;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ViolinDao {
  private MongoTemplate mongoTemplate;
  private ResolutionService resolutionService;

  public List<ViolinDto> getViolinDtos(Experiment e, Resolution r, List<String> geneCodes) {
    MatchOperation matchCellExpression = Aggregation.match(Criteria.where("experimentId").is(e.getId())
        .and("geneCode").in(geneCodes));
    UnwindOperation unwindExpressions = Aggregation.unwind("$expressions");

    GroupOperation groupByCell = Aggregation.group("expressions.cellId")
        .push(new BasicDBObject("geneCode", "$geneCode")
            .append("expression", "$expressions.expression"))
        .as("expressions");

    LookupOperation lookupCells = LookupOperation.newLookup()
        .from("cell")
        .localField("_id")
        .foreignField("_id")
        .as("cellInfo");

    LookupOperation lookupClusters = LookupOperation.newLookup()
        .from("cluster")
        .localField("cellInfo.clusterIds")
        .foreignField("_id")
        .as("clusterInfo");

    UnwindOperation unwindClusterInfo = Aggregation.unwind("$clusterInfo");

    MatchOperation matchCluster = Aggregation.match(Criteria.where("clusterInfo.resolution").is(r.getId()));

    ProjectionOperation projectIds = Aggregation.project("expressions")
        .and("clusterInfo._id").as("clusterId")
        .and("cellInfo.sample").as("sampleId");

    GroupOperation groupByClusterAndSample = Aggregation.group("clusterId", "sampleId")
        .push("expressions").as("expressions");

    LookupOperation lookupCellCount = LookupOperation.newLookup()
        .from("cell")
        .localField("_id.clusterId")
        .foreignField("clusterIds")
        .pipeline(Aggregation.group()
            .count().as("cellCount"))
        .as("cellCount");

    ProjectionOperation projectCellCount = Aggregation.project()
        .and("expressions").as("expressionLists")
        .and("cellCount.cellCount").as("cellCount")
        .and("_id.clusterId").as("clusterId")
        .and("_id.sampleId").as("sampleId");

    UnwindOperation unwindSampleId = Aggregation.unwind("$sampleId");
    UnwindOperation unwindCellCount = Aggregation.unwind("$cellCount");

    Aggregation aggregation = Aggregation.newAggregation(
        matchCellExpression,
        unwindExpressions,
        groupByCell,
        lookupCells,
        lookupClusters,
        unwindClusterInfo,
        matchCluster,
        projectIds,
        groupByClusterAndSample,
        lookupCellCount,
        projectCellCount,
        unwindSampleId,
        unwindCellCount);

    long start = System.nanoTime();
    List<ViolinQueryDto> result = mongoTemplate
        .aggregate(aggregation, "cellExpressionList", ViolinQueryDto.class)
        .getMappedResults();
    long end = System.nanoTime();
    if (result.isEmpty())
      throw new NoObjectFoundException("Could not get violin dtos for experiment: " + e.getId());
    else {
      /*
       * List<ViolinDto> resultDtos = result.stream().map(dto -> {
       * ViolinDto resultDto = new ViolinDto();
       * resultDto.setClusterId(dto.getClusterId());
       * resultDto.setSampleId(dto.getSampleId());
       * dto.getExpressionLists().stream().forEach(cell -> {
       * geneCodes.stream().forEach(gc -> {
       * Optional<GeneExpression> expressionOpt = cell.stream()
       * .filter(ex -> ex.getGeneCode().equals(gc)).findFirst();
       * if (expressionOpt.isPresent()) {
       * resultDto.getExpressions().add(expressionOpt.get());
       * } else {
       * resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
       * }
       * });
       * });
       * for (int i = 0; i < (dto.getCellCount() - dto.getExpressionLists().size());
       * i++) {
       * geneCodes.stream().forEach(gc -> {
       * resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
       * });
       * }
       * return resultDto;
       * }).toList();
       * log.info("Completed query in " + (end - start) / 1_000_000_000.0 +
       * " seconds");
       */
      // return resultDtos;
      return null;

    }

  }

  public List<ViolinDto> getDtosByResolution(Experiment e, List<String> codes, Resolution r) {
    MatchOperation matchExpressions = Aggregation.match(Criteria.where("resolutionId").is(new ObjectId(r.getId()))
        .and("code").in(codes));
    LookupOperation lookupCluster = Aggregation.lookup(
      "cluster",
      "clusterId",
      "_id",
      "clusterInfo"
    );

    LookupOperation lookupSample = Aggregation.lookup(
      "sample",
      "sampleId",
      "_id",
      "sampleInfo"
    );

    UnwindOperation unwindCluster = Aggregation.unwind("clusterInfo");
    UnwindOperation unwindSample = Aggregation.unwind("sampleInfo");

   ProjectionOperation project = Aggregation.project("expressions", "code")
    .and("sampleInfo.name").as("sample")
    .and("clusterInfo.name").as("cluster");

    Aggregation aggregation = Aggregation.newAggregation(
      matchExpressions,
      lookupCluster,
      lookupSample,
      unwindCluster,
      unwindSample,
      project
    );

    List<ViolinDto> result = mongoTemplate.aggregate(aggregation, "violinGroup", ViolinDto.class).getMappedResults();
    return result;
  }

}
