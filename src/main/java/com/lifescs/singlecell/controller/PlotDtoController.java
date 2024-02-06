package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.model.LowDimentionalDtoByResolution;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.PlotService;

@RestController
public class PlotDtoController {
    private PlotService plotService;
    private Logger logger;

    public PlotDtoController(PlotService plotService) {
        this.plotService = plotService;
        this.logger = LoggerFactory.getLogger(PlotDtoController.class);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/lowdim")
    public LowDimentionalDtoByResolution getDto() {
        Experiment testExperiment = new Experiment("Test experiment");
        testExperiment.setId("exp1");
        Resolution testResolution = new Resolution();
        testResolution.setId("exp1cluster_0.20");
        LowDimentionalDtoByResolution dto = plotService.getLowDimentionalDtoByResolution(testExperiment,
                testResolution);
        // logger.info(dto.getClusterNames().toString());
        return dto;
    }
}
