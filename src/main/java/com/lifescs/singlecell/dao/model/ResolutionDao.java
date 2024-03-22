package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.query.TopMarkerDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.ResolutionRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ResolutionDao {
  private MongoTemplate mongoTemplate;
  private ResolutionRepository repository;
  private ClusterDao clusterDao;

  public Optional<Resolution> findResolutionById(String id) {
    return repository.findById(id);
  }

  public void deleteResolution(Resolution r) {
    clusterDao.deleteClustersByResolution(r);
    Query query = Query.query(Criteria.where("_id").is(new ObjectId(r.getId())));
    mongoTemplate.updateFirst(query, Update.update("deleted", true), "resolution");
    log.info("Deleted resolution: " + r.getId());
  }

  public void deleteResolutionsByExperiment(Experiment e) {
    findResolutionsByExperiment(e).stream().forEach(
        r -> deleteResolution(r));
  }

  public List<Resolution> findResolutionsByExperiment(Experiment e) {
    Query query = Query.query(Criteria.where("experiment.$id").is(e.getId()));
    return mongoTemplate.find(query, Resolution.class);
  }

  public void saveResolution(Resolution r) {
    repository.save(r);
  }

  public void saveResolutions(List<Resolution> rl) {
    repository.saveAll(rl);
  }

  // TODO check if this is correct
  // Assumes markers are ordered in descending order in each cluster
  public List<TopMarkerDto> getTopMarkers(Resolution r, Integer markersPerCluster) {
    MatchOperation matchClusters = Aggregation.match(Criteria.where("resolution.$id").is(new ObjectId(r.getId())));

    ProjectionOperation projectSlice = Aggregation.project()
        .and("_id").as("clusterId")
        .and("markers.geneCode").slice(markersPerCluster).as("topMarkers");

    Aggregation aggregation = Aggregation.newAggregation(
        matchClusters,
        projectSlice);

    List<TopMarkerDto> result = mongoTemplate.aggregate(aggregation, "cluster", TopMarkerDto.class)
        .getMappedResults();
    if (result.isEmpty())
      throw new NoObjectFoundException("No results for top markers in resolution: " + r.getId());
    else
      return result;
  }

  public void clean() {
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "resolution");
    clusterDao.clean();
  }

public Optional<Resolution> findResolutionByName(String name) {
    return repository.findByName(name);
}

}
