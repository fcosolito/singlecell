package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.CellRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CellDao {
        private MongoTemplate mongoTemplate;
        private CellRepository cellRepository;

        class MarkerExpressionResult {
                private List<GeneExpression> expressionList;
                private List<String> clusterMarkers;
        }

        // TODO must get the expressions from every marker in the resolution, not just
        // its cluster
        // For every cell in the same resolution the expression count must be the same
        // Write tests?
        public List<GeneExpression> getMarkerExpressionsForResolution(Cell c, Resolution r) {
                MatchOperation matchCell = Aggregation.match(Criteria.where("_id").is(c.getId()));

                LookupOperation lookupCluster = LookupOperation.newLookup()
                                .from("cluster")
                                .localField("clusterIds")
                                .foreignField("_id")
                                .as("clusterInfo");

                UnwindOperation unwindClusterInfo = Aggregation.unwind("clusterInfo");
                MatchOperation matchCluster = Aggregation.match(Criteria.where("clusterInfo.resolution").is(r.getId()));

                ProjectionOperation projectMarkerNames = Aggregation.project("barcode", "geneExpressionId")
                                .and("clusterInfo.markers.geneCode").as("markers");

                LookupOperation lookupGeneExpression = LookupOperation.newLookup()
                                .from("geneExpressionList")
                                .localField("geneExpressionId")
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
                        List<GeneExpression> expressions = result.get(0).expressionList;
                        List<String> foundMarkers = expressions.stream()
                                        .map(e -> e.getGeneCode()).collect(Collectors.toList());
                        result.get(0).clusterMarkers.stream()
                                        .filter(m -> foundMarkers.contains(m)).map(m -> new GeneExpression(m, 0.0))
                                        .forEach(e -> expressions.add((GeneExpression) e));

                        return expressions;

                }

        }

        public Optional<Cell> findCellById(String id) {
                return cellRepository.findById(id);
        }

        public List<Cluster> getCellClusters(Cell c) throws NoObjectFoundException {
                MatchOperation matchCell = Aggregation.match(Criteria.where("_id").is(c.getId()));
                UnwindOperation unwindCluster = Aggregation.unwind("clusters");
                UnwindOperation unwindClusterInfo = Aggregation.unwind("clusterInfo");

                LookupOperation lookupCluster = LookupOperation.newLookup()
                                .from("cluster")
                                .localField("clusters")
                                .foreignField("_id")
                                .as("clusterInfo");

                ProjectionOperation projectCluster = Aggregation.project("clusterInfo");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchCell,
                                unwindCluster,
                                lookupCluster,
                                projectCluster,
                                unwindClusterInfo);
                List<ClusterResult> result = mongoTemplate.aggregate(aggregation, "cell", ClusterResult.class)
                                .getMappedResults();
                if (result.isEmpty())
                        throw new NoObjectFoundException("No cluster was found for cell with cell id: " + c.getId());
                else
                        return result.stream().map(r -> r.cluster).toList();

        }

        class ClusterResult {
                private Cluster cluster;
        }
}
