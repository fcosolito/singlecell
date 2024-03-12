package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.PlotService;
import com.lifescs.singlecell.service.ResolutionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class PlotDtoController {
    private PlotService plotService;
    private ExperimentService experimentService;
    private ResolutionService resolutionService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/low_dimensional/by_resolution")
    public LowDimensionalDto getDtoByResolution() {
        //Experiment testExperiment = experimentService.findExperimentById("exp1").get();
        //Resolution testResolution = experimentService.findResolutionById("exp1cluster_0.20").get();
        //LowDimensionalDto dto = plotService.getLowDimensionalDtoByResolution(testExperiment,
                //testResolution);
        // log.info(dto.getClusterNames().toString());
        //return dto;
        return null;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/low_dimensional/by_gene")
    public LowDimensionalDto getDtoByGene() {
        Experiment e = experimentService.findExperimentById("exp1").get();

        List<String> searchList = new ArrayList<>();
        searchList.add("Tram1");
        searchList.add("Vcpip1");
        searchList.add("Naaa");
        searchList.add("Tubab1");

        LowDimensionalDto dto = plotService.getLowDimensionalDtoByGeneCodes(e,
                searchList);

        // log.info(dto.getClusterNames().toString());
        return dto;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/heatmap/{resolutionId}")
    public List<HeatmapDto> getHeatmap(@PathVariable String resolutionId) {
        Resolution r = resolutionService.findResolutionById(resolutionId).get();
        return plotService.getHeatmapDtos(r);

    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/plots/violin")
    public List<ViolinDto> getViolin() {
        Experiment e = experimentService.findExperimentById("exp1").get();
        List<String> searchList = new ArrayList<>();
        searchList.add("Tram1");
        searchList.add("Vcpip1");
        //return plotService.getViolinDtos(e, e.getResolutions().get(3), searchList);
        return null;

    }
    // @ExceptionHandler

}
