package com.lifescs.singlecell.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.PlotDao;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByResolution;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

@Service
public class PlotService {
    private PlotDao plotDao;
    private Logger logger;

    public PlotService(PlotDao plotDao) {
        this.plotDao = plotDao;
        this.logger = LoggerFactory.getLogger(PlotService.class);
    }

    public LowDimentionalDtoByResolution getLowDimentionalDtoByResolution(Experiment e, Resolution r) {
        return plotDao.getLowDimentionalDtoByResolution2(e, r);
    }
}
