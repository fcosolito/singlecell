package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
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
    private CellExpressionListDao cellExpressionListDao;

    public Experiment saveExperiment(Experiment e) {
        return repository.save(e);
    }

    public Optional<Experiment> findExperimentById(String id) {
        return repository.findById(id);
    }

    public void deleteExperiment(Experiment e) {
        sampleDao.deleteSamplesByExperiment(e);
        cellExpressionListDao.deleteListsByExperiment(e);
        cellDao.deleteCellsByExperiment(e);
        geneExpressionListDao.deleteListsByExperiment(e);
        resolutionDao.deleteResolutionsByExperiment(e);

    }

}
