package com.lifescs.singlecell.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Sample;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
// Class to save and update experiment data
public class ExperimentService {
    private ExperimentDao experimentDao;
    private CellDao cellDao;

    // Saves the experiment object and the objects related with document references,
    // does not save gene/protein/marker expressions
    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperiment(e);
    }

    public void removeExperiment(String experimentId) {
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

    public void saveSamples(List<Sample> samples) {
        experimentDao.saveSamples(samples);
    }

    public void saveCells(List<Cell> cells) {
        cellDao.saveCells(cells);
    }

}
