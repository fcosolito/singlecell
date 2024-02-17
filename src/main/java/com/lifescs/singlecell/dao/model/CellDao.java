package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.model.MarkerExpressionResult;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CellDao {
        private MongoTemplate mongoTemplate;

        public List<GeneExpression> getMarkerExpressions(Cell c, Resolution r) {
                MatchOperation matchCell = Aggregation.match(Criteria.where("_id").is(c.getId()));

                LookupOperation lookupCluster = LookupOperation.newLookup()
                                .from("cluster")
                                .localField("clusters")
                                .foreignField("_id")
                                .as("clusterInfo");

                UnwindOperation unwindClusterInfo = Aggregation.unwind("clusterInfo");
                MatchOperation matchCluster = Aggregation.match(Criteria.where("clusterInfo.resolution").is(r.getId()));

                ProjectionOperation projectMarkerNames = Aggregation.project("barcode", "geneExpressions")
                                .and("clusterInfo.markers.geneCode").as("markers");

                LookupOperation lookupGeneExpression = LookupOperation.newLookup()
                                .from("geneExpressionList")
                                .localField("geneExpressions")
                                .foreignField("_id")
                                .as("geneExpressionInfo");

                UnwindOperation unwindGeneExpressionInfo = Aggregation.unwind("geneExpressionInfo");

                ProjectionOperation projectFilterExpressions = Aggregation.project()
                                .and("markers").as("clusterMarkers")
                                .and(ArrayOperators.Filter.filter("geneExpressionInfo.geneExpressions")
                                                .as("geneExp")
                                                .by(ArrayOperators.In.arrayOf("markers")
                                                                .containsValue("$$geneExp.geneCode")))
                                .as("expressionList");

                UnwindOperation unwindExpressionList = Aggregation.unwind("expressionList");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchCell,
                                lookupCluster,
                                unwindClusterInfo,
                                matchCluster,
                                projectMarkerNames,
                                lookupGeneExpression,
                                unwindGeneExpressionInfo,
                                projectFilterExpressions

                );
                List<MarkerExpressionResult> result = mongoTemplate
                                .aggregate(aggregation, "cell", MarkerExpressionResult.class)
                                .getMappedResults();

                // Add null expressions
                if (result.isEmpty())
                        return null;
                else {
                        List<GeneExpression> expressions = result.get(0).getExpressionList();
                        List<String> foundMarkers = expressions.stream()
                                        .map(e -> e.getGeneCode()).collect(Collectors.toList());
                        result.get(0).getClusterMarkers().stream()
                                        .filter(m -> foundMarkers.contains(m)).map(m -> new GeneExpression(m, 0.0))
                                        .forEach(e -> expressions.add(e));

                        return expressions;

                }

        }

}
