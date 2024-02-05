package com.lifescs.singlecell.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.PlotDao;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByResolution;

@Service
public class PlotService {
    private PlotDao plotDao;
    private Logger logger;

    public PlotService(PlotDao plotDao) {
        this.plotDao = plotDao;
        this.logger = LoggerFactory.getLogger(PlotService.class);
    }

    public LowDimentionalDtoByResolution getLowDimentionalDtoByResolution(String experimentId) {
        return plotDao.getLowDimentionalDtoByResolution(experimentId);
    }
}
