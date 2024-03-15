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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.ClusterRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClusterDao {
        private MongoTemplate mongoTemplate;
        private ClusterRepository clusterRepository;
        private HeatmapClusterDao heatmapClusterDao;

        public Optional<Cluster> findClusterById(String id) {
                return clusterRepository.findById(id);
        }

        public List<Cluster> saveClusters(List<Cluster> cl) {
                return (List<Cluster>) clusterRepository.saveAll(cl);
        }

        public List<Cluster> findClustersByResolution(Resolution r) {
                Query query = Query.query(Criteria.where("resolution.$id").is(r.getId()));
                return mongoTemplate.find(query, Cluster.class);
        }

        public void deleteClustersByResolution(Resolution r) {
                heatmapClusterDao.deleteHeatmapClustersbyResolution(r);
                Query query = Query.query(Criteria.where("resolution.$id").is(r.getId()));
                mongoTemplate.remove(query, Cluster.class);
        }

        public List<Cluster> findClustersByExperiment(Experiment experiment) {
                LookupOperation lookupResolution = Aggregation.lookup(
                                "resolution",
                                "resolution.$id",
                                "_id",
                                "resolutionInfo");

                MatchOperation matchExperiment = Aggregation.match(
                                Criteria.where("resolutionInfo.experiment.$id")
                                                .is(experiment.getId()));

                ProjectionOperation projectCluster = Aggregation.project(
                                "id", "name", "markers", "resolution");

                Aggregation aggregation = Aggregation.newAggregation(
                                lookupResolution,
                                matchExperiment,
                                projectCluster);

                List<Cluster> result = mongoTemplate.aggregate(aggregation, "cluster", Cluster.class)
                                .getMappedResults();
                if (result.isEmpty())
                        throw new NoObjectFoundException("Clusters not found for experiment: " + experiment.getId());
                else
                        return result;
        }

        // TODO test it
        public List<HeatmapClusterLoadDto> getMarkerExpressionsForCluster(Cluster c,
                        List<String> geneCodes)
                        throws NoObjectFoundException {

                LookupOperation lookupCells = LookupOperation.newLookup()
                                .from("cell")
                                .localField("cell.$id")
                                .foreignField("_id")
                                .as("cellInfo");

                MatchOperation matchCluster = Aggregation.match(Criteria.where("cellInfo.cellClusters")
                                .elemMatch(Criteria.where("cluster.$id").is(c.getId())));

                ProjectionOperation projectBarcodeAndExpressions = Aggregation.project()
                                .and("cellInfo.barcode").as("barcode")
                                .and(ArrayOperators.Filter.filter("expressions")
                                                .as("expression")
                                                .by(ArrayOperators.In.arrayOf(geneCodes)
                                                                .containsValue("$$expression.code")))
                                .as("expressions");

                Aggregation aggregation = Aggregation.newAggregation(
                                lookupCells,
                                matchCluster,
                                projectBarcodeAndExpressions

                );

                List<HeatmapClusterLoadDto> result = mongoTemplate
                                .aggregate(aggregation, "cellExpressionList", HeatmapClusterLoadDto.class)
                                .getMappedResults();
                if (result.isEmpty())
                        throw new NoObjectFoundException("No marker expressions found for cluster: " + c.getId());
                else
                        return result;
        }

}
