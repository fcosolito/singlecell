package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.model.HeatmapDto;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByGene;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByResolution;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.PlotService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class PlotDtoController {
    private PlotService plotService;
    private ExperimentService experimentService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/low_dimentional/by_resolution")
    public LowDimentionalDtoByResolution getDtoByResolution() {
        Experiment testExperiment = new Experiment("Test experiment");
        testExperiment.setId("exp1");
        Resolution testResolution = new Resolution();
        testResolution.setId("exp1cluster_0.20");
        LowDimentionalDtoByResolution dto = plotService.getLowDimentionalDtoByResolution(testExperiment,
                testResolution);
        // log.info(dto.getClusterNames().toString());
        return dto;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/low_dimentional/by_gene")
    public LowDimentionalDtoByGene getDtoByGene() {
        Experiment testExperiment = new Experiment("Test experiment");
        testExperiment.setId("exp1");

        List<String> searchList = new ArrayList<>();
        searchList.add("Tram1");
        searchList.add("Vcpip1");

        LowDimentionalDtoByGene dto = plotService.getLowDimentionalDtoByGeneCodes(testExperiment,
                searchList);

        // log.info(dto.getClusterNames().toString());
        return dto;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/heatmap")
    public List<HeatmapDto> getHeatmap() {
        Experiment e = experimentService.findExperimentById("exp1").get();
        return plotService.getHeatmapDtos(e.getResolutions().get(3));

    }

    // @ExceptionHandler

}
