package com.lifescs.singlecell.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
// Class to save and update experiment data
public class ExperimentService {
    private ExperimentDao experimentDao;

    // Saves the experiment object and the objects related with document references,
    // does not save gene/protein/marker expressions
    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperimentDeep(e);
    }

    public void removeExperiment(String experimentId) {
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

}
