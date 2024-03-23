package com.lifescs.singlecell.dao.plot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.Resolution;
import com.mongodb.BasicDBObject;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class LowDimensionalDao {
  private MongoTemplate mongoTemplate;

  public LowDimensionalDto getDtoBySamples(Experiment e) {
    MatchOperation matchCells = Aggregation.match(Criteria.where("experiment.$id").is(e.getId()));
    LookupOperation lookupSample = Aggregation.lookup(
        "sample",
        "sample.$id",
        "_id",
        "sampleInfo");
    ProjectionOperation projectMetadata = Aggregation.project(
        "spring1", "spring2", "tsne1", "tsne2", "pca1", "pca2", "umap1", "umap2", "barcode")
        .and("sampleInfo.name").as("sample");
    GroupOperation groupByExperiment = Aggregation.group()
        .push("barcode").as("barcodes")
        .push("sample").as("samples")
        .push("spring1").as("spring1")
        .push("spring2").as("spring2")
        .push("umap1").as("umap1")
        .push("umap2").as("umap2")
        .push("pca1").as("pca1")
        .push("pca2").as("pca2")
        .push("tsne1").as("tsne1")
        .push("tsne2").as("tsne2");

    Aggregation aggregation = Aggregation.newAggregation(
      matchCells,
      lookupSample,
      projectMetadata,
      groupByExperiment
    );
    return mongoTemplate.aggregate(aggregation, "cell", LowDimensionalDto.class).getUniqueMappedResult();
  }

  public LowDimensionalDto getDtoByResolution(Experiment e, Resolution r){
    MatchOperation matchExperiment = Aggregation.match(Criteria.where("experiment.$id").is(e.getId()));

    UnwindOperation unwindCellClusters = Aggregation.unwind("cellClusters");

    MatchOperation matchResolution = Aggregation.match(Criteria.where("cellClusters.resolution.$id").is(new ObjectId(r.getId())));
    LookupOperation lookupCluster = Aggregation.lookup(
      "cluster",
      "cellClusters.cluster.$id",
      "_id",
      "clusterInfo"
    );
    UnwindOperation unwindCluster = Aggregation.unwind("clusterInfo");

    ProjectionOperation projectMetadata = Aggregation.project(
        "spring1", "spring2", "tsne1", "tsne2", "pca1", "pca2", "umap1", "umap2", "barcode")
        .and("clusterInfo.name").as("cluster");

    GroupOperation groupByExperiment = Aggregation.group()
        .push("barcode").as("barcodes")
        .push("cluster").as("clusters")
        .push("spring1").as("spring1")
        .push("spring2").as("spring2")
        .push("umap1").as("umap1")
        .push("umap2").as("umap2")
        .push("pca1").as("pca1")
        .push("pca2").as("pca2")
        .push("tsne1").as("tsne1")
        .push("tsne2").as("tsne2");

    Aggregation aggregation = Aggregation.newAggregation(
      matchExperiment,
      unwindCellClusters,
      matchResolution,
      lookupCluster,
      unwindCluster,
      projectMetadata,
      groupByExperiment
    );
    return mongoTemplate.aggregate(aggregation, "cell", LowDimensionalDto.class).getUniqueMappedResult();
  }


  // A map from cell barcodes to their sum of expressions for the passed codes
  public Map<String, Double> getExpressionSumMap(Experiment e, List<String> codes) {
    MatchOperation matchGeneExpressions = Aggregation.match(Criteria.where("experiment.$id").is(e.getId())
        .and("code").in(codes));

    UnwindOperation unwindExpressions = Aggregation.unwind("expressions");

    LookupOperation lookupCells = Aggregation.lookup(
        "cell",
        "expressions.cell.$id",
        "_id",
        "cellInfo");

    ProjectionOperation projectBarcode = Aggregation.project("experiment", "code")
        .and("expressions.expression").as("expression")
        .and("cellInfo.barcode").as("barcode");

    GroupOperation groupByExperimentAndCode = Aggregation.group("experiment", "code")
        .push(new BasicDBObject("barcode", "$barcode")
            .append("expression", "$expression"))
        .as("expressions");

    Aggregation aggregation = Aggregation.newAggregation(
        matchGeneExpressions,
        unwindExpressions,
        lookupCells,
        projectBarcode,
        groupByExperimentAndCode);

    long start = System.nanoTime();
    List<ResultByGene> result = mongoTemplate.aggregate(aggregation, "geneExpressionList", ResultByGene.class)
        .getMappedResults();
    long end = System.nanoTime();
    log.info("Low dimensional query: " + (end - start) / 1_000_000_000.0 + " seconds");

    Map<String, Double> barcode2expressionSum = new HashMap<>();
    result.stream().forEach(
        r -> {
          r.expressions.stream().forEach(
              expression -> {
                Double value = barcode2expressionSum.get(expression.barcode);
                if (value == null)
                  barcode2expressionSum.put(expression.barcode, expression.expression);
                else
                  barcode2expressionSum.put(expression.barcode, expression.expression + value);
              });
        });

    return barcode2expressionSum;

  }

  class ResultByGene {
    String code;
    List<ResultExpression> expressions;

    class ResultExpression {
      String barcode;
      Double expression;
    }
  }

}
