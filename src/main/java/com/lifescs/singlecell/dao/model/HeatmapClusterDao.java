package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.HeatmapClusterRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class HeatmapClusterDao {
    private HeatmapClusterRepository repository;
    private MongoTemplate mongoTemplate;

    public void saveAll(List<HeatmapCluster> list) {
        repository.saveAll(list);
    }

    public List<HeatmapCluster> findHeatmapClustersByResolution(Resolution r) {
        LookupOperation lookupCluster = Aggregation.lookup(
                "cluster",
                "cluster.$id",
                "_id",
                "clusterInfo");

        MatchOperation matchResolution = Aggregation.match(
                Criteria.where("clusterInfo.resolution.$id").is(r.getId()));

        Aggregation aggregation = Aggregation.newAggregation(
                lookupCluster,
                matchResolution);

        List<HeatmapCluster> result = mongoTemplate
                .aggregate(aggregation, "heatmapClusters", HeatmapCluster.class).getMappedResults();
        if (result.isEmpty())
            throw new NoObjectFoundException("No heatmap clusters found for resolution: " + r.getId());
        else
            return result;

    }

    // Should only be called by ClusterDao.deleteClustersByResolution method
    public void deleteHeatmapClustersbyResolution(Resolution r) {
        repository.deleteAll(findHeatmapClustersByResolution(r));
    }

}
