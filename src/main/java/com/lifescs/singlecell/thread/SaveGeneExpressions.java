package com.lifescs.singlecell.thread;

import java.util.List;

import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveGeneExpressions implements Runnable {
    private GeneExpressionListDao geneExpressionListDao;
    private final Experiment experiment;
    private final List<GeneExpressionDto> expressionList;

    @Override
    public void run() {
        geneExpressionListDao.bulkSaveExpressions(experiment, expressionList);
    }

}
