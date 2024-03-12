package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.repository.ClusterRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClusterDao {
        private MongoTemplate mongoTemplate;
        private ClusterRepository clusterRepository;

        public Optional<Cluster> findClusterById(String id) {
                return clusterRepository.findById(id);
        }

        public List<Cluster> saveClusters(List<Cluster> cl) {
                return (List<Cluster>) clusterRepository.saveAll(cl);
        }

        // TODO
        public List<Cluster> findClustersByExperiment(Experiment experiment) {
                return null;
        }

        public List<HeatmapClusterLoadDto> getMarkerExpressionsForCluster(Cluster c,
                        List<String> geneCodes)
                        throws NoObjectFoundException {
                MatchOperation matchCells = Aggregation.match(Criteria.where("clusterIds").is(c.getId()));

                LookupOperation lookupExpressions = LookupOperation.newLookup()
                                .from("geneExpressionList")
                                .localField("geneExpressionId")
                                .foreignField("_id")
                                .as("expressionInfo");

                UnwindOperation unwindExpressions = Aggregation.unwind("expressionInfo");

                ProjectionOperation projectBarcodeAndExpressions = Aggregation.project("barcode")
                                .and(ArrayOperators.Filter.filter("expressionInfo.geneExpressions")
                                                .as("geneExp")
                                                .by(ArrayOperators.In.arrayOf(geneCodes)
                                                                .containsValue("$$geneExp.geneCode")))
                                .as("expressions");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchCells,
                                lookupExpressions,
                                unwindExpressions,
                                projectBarcodeAndExpressions

                );

                List<HeatmapClusterLoadDto> result = mongoTemplate
                                .aggregate(aggregation, "cell", HeatmapClusterLoadDto.class)
                                .getMappedResults();
                if (result.isEmpty())
                        throw new NoObjectFoundException("No marker expressions found for cluster: " + c.getId());
                else
                        return result;
        }

}
