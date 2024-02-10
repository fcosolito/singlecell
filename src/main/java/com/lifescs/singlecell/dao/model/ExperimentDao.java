package com.lifescs.singlecell.dao.model;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;
import com.lifescs.singlecell.repository.CellRepository;
import com.lifescs.singlecell.repository.ClusterRepository;
import com.lifescs.singlecell.repository.ExperimentRepository;
import com.lifescs.singlecell.repository.GeneExpressionRepository;
import com.lifescs.singlecell.repository.ResolutionRepository;
import com.lifescs.singlecell.repository.SampleRepository;

@Component
public class ExperimentDao {
    private SampleRepository sampleRepository;
    private ExperimentRepository experimentRepository;
    private CellRepository cellRepository;
    private ResolutionRepository resolutionRepository;
    private ClusterRepository clusterRepository;
    private GeneExpressionRepository geneExpressionRepository;
    private Logger logger;

    public ExperimentDao(SampleRepository sampleRepository, ExperimentRepository experimentRepository,
            CellRepository cellRepository, ResolutionRepository resolutionRepository,
            ClusterRepository clusterRepository, GeneExpressionRepository geneExpressionRepository) {
        this.logger = LoggerFactory.getLogger(ExperimentDao.class);
        this.sampleRepository = sampleRepository;
        this.experimentRepository = experimentRepository;
        this.cellRepository = cellRepository;
        this.resolutionRepository = resolutionRepository;
        this.clusterRepository = clusterRepository;
        this.geneExpressionRepository = geneExpressionRepository;
    }

    public Experiment saveExperimentDeep(Experiment e) {
        e.getResolutions().stream()
                .forEach(r -> {
                    saveResolution(r);
                    logger.info("Saving resolution: " + r.getId());
                });
        e.getSamples().stream()
                .forEach(s -> {
                    saveSample(s);
                    logger.info("Saving sample: " + s.getId());
                });
        e.getCells().stream()
                .forEach(c -> {
                    saveCell(c);
                });
        return experimentRepository.save(e);
    }

    // Does not update any referenced objects
    public Experiment saveExperiment(Experiment e) {
        return experimentRepository.save(e);
    }

    public Cell saveCell(Cell c) {

        geneExpressionRepository.saveAll(c.getGeneExpressions());
        return cellRepository.save(c);
    }

    public Cluster saveCluster(Cluster cl) {
        return clusterRepository.save(cl);
    }

    public Sample saveSample(Sample s) {
        return sampleRepository.save(s);

    }

    public Resolution saveResolution(Resolution r) {
        r.getClusters().stream()
                .forEach(c -> {
                    saveCluster(c);
                });
        return resolutionRepository.save(r);
    }

    public Optional<Sample> findSampleByName(String name) {
        return sampleRepository.findByName(name);
    }

    public Optional<Experiment> findExperimentById(String id) {
        return experimentRepository.findById(id);
    }

    public Optional<Cluster> findClusterById(String id) {
        return clusterRepository.findById(id);
    }

}
