package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;
import com.lifescs.singlecell.repository.CellRepository;
import com.lifescs.singlecell.repository.ClusterRepository;
import com.lifescs.singlecell.repository.ExperimentRepository;
import com.lifescs.singlecell.repository.ResolutionRepository;
import com.lifescs.singlecell.repository.SampleRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ExperimentDao {
    private MongoTemplate mongoTemplate;
    private SampleRepository sampleRepository;
    private ExperimentRepository experimentRepository;

    // Does not update any referenced objects
    public Experiment saveExperiment(Experiment e) {
        return experimentRepository.save(e);
    }

    public void saveSamples(List<Sample> sl) {
        sampleRepository.saveAll(sl);

    }

    public Optional<Sample> findSampleByName(String name) {
        return sampleRepository.findByName(name);
    }

    public Optional<Experiment> findExperimentById(String id) {
        return experimentRepository.findById(id);
    }

}
