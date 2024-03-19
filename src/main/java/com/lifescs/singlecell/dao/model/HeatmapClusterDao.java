package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.HeatmapClusterRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
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
        Criteria.where("clusterInfo.resolution.$id").is(new ObjectId(r.getId())));

    Aggregation aggregation = Aggregation.newAggregation(
        lookupCluster,
        matchResolution);

    List<HeatmapCluster> result = mongoTemplate
        .aggregate(aggregation, "heatmapClusters", HeatmapCluster.class).getMappedResults();
    return result;

  }

  public void deleteHeatmapClusterbyCluster(Cluster cluster) {
    Query query = Query.query(Criteria.where("cluster.$id").is(new ObjectId(cluster.getId())));
    mongoTemplate.updateMulti(query, Update.update("deleted", true), "heatmapCluster");
  }

public void clean() {
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "heatmapCluster");
}

}
