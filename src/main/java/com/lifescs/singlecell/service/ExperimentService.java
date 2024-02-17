package com.lifescs.singlecell.service;

import com.lifescs.singlecell.model.GeneExpression;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.MarkerExpressionList;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
// Class to save and update experiment data
public class ExperimentService {
    private ExperimentDao experimentDao;
    private CellDao cellDao;

    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperiment(e);
    }

    // Saves the experiment object and every related object like cells, gene
    // expressions, etc
    public Experiment saveExperimentDeep(Experiment e) {
        return experimentDao.saveExperimentDeep(e);
    }

    public void setResolutionMarkerExpressions(Experiment e, Resolution r) {
        // Try with parallel stream ?
        for (Cell c : e.getCells()) {
            setCellMarkerExpressions(c, r);
            experimentDao.saveCell(c);
        }
    }

    public Cell setCellMarkerExpressions(Cell c, Resolution r) {
        List<GeneExpression> markerExpressions = cellDao.getMarkerExpressions(c, r);
        MarkerExpressionList markerList = new MarkerExpressionList();
        markerList.setMarkerExpressions(markerExpressions);
        markerList.setResolution(r);
        c.getMarkerExpressions().add(markerList);
        return c;
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

}
