package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.repository.ExperimentRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ExperimentDao {
  private MongoTemplate mongoTemplate;
  private ExperimentRepository repository;
  private CellDao cellDao;
  private ResolutionDao resolutionDao;
  private SampleDao sampleDao;
  private GeneExpressionListDao geneExpressionListDao;

  public Experiment saveExperiment(Experiment e) {
    return repository.save(e);
  }

  public Optional<Experiment> findExperimentById(String id) {
    return repository.findById(id);
  }

  public void deleteExperiment(Experiment e) {
    Query query = Query.query(Criteria.where("_id").is(e.getId()));
    mongoTemplate.updateFirst(query, Update.update("deleted", true), "experiment");

    sampleDao.deleteSamplesByExperiment(e);
    cellDao.deleteCellsByExperiment(e);
    geneExpressionListDao.deleteListsByExperiment(e);
    resolutionDao.deleteResolutionsByExperiment(e);

  }

  public void cleanDeletedExperiments(){
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "experiment");

    sampleDao.clean();
    cellDao.clean();
    geneExpressionListDao.clean();
    resolutionDao.clean();
  }

}
