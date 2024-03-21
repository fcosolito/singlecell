package com.lifescs.singlecell.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.HeatmapService;
import com.lifescs.singlecell.service.ResolutionService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class HeatmapController {
  private HeatmapService heatmapService;
  private ResolutionService resolutionService;
  private ExperimentService experimentService;

  @CrossOrigin
  @GetMapping("/experiment/{id}/plots/heatmap")
  public List<HeatmapDto> getDtoByResolution(@PathVariable String experimentId) {
    Experiment experiment = experimentService.findExperimentById(experimentId).get();
    Resolution resolution = resolutionService.findResolutionsByExperiment(experiment).get(3); 
    return heatmapService.getHeatmapByResolution(resolution);
  }

}
