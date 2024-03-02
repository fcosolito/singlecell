package com.lifescs.singlecell.thread;

import java.util.List;

import com.lifescs.singlecell.dao.model.CellExpressionListDao;
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SaveCellExpressions implements Runnable {
    private CellExpressionListDao cellExpressionListDao;
    private final Experiment experiment;
    private final List<GeneExpressionDto> expressionList;

    @Override
    public void run() {
        cellExpressionListDao.bulkSaveExpressions(experiment, expressionList);
    }

}
