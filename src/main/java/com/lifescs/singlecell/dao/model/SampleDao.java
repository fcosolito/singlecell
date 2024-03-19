package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Sample;
import com.lifescs.singlecell.repository.SampleRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class SampleDao {
  private SampleRepository repository;
  private MongoTemplate mongoTemplate;

  public void deleteSamplesByExperiment(Experiment e) {
    Query query = Query.query(Criteria.where("experiment.$id").is(e.getId()));
    mongoTemplate.updateMulti(query, Update.update("deleted", true), "sample");
  }

  public void saveSamples(List<Sample> sl) {
    repository.saveAll(sl);

  }

  public Optional<Sample> findSampleByName(String name) {
    return repository.findByName(name);
  }

  public void clean() {
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "sample");

  }

}
