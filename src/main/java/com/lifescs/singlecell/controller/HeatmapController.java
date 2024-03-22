package com.lifescs.singlecell.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.HeatmapService;
import com.lifescs.singlecell.service.ResolutionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class HeatmapController {
  private HeatmapService heatmapService;
  private ResolutionService resolutionService;
  private ExperimentService experimentService;

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/heatmap")
  public List<HeatmapDto> getDtoByResolution(@PathVariable(name = "id") String experimentId, @RequestParam(name = "res") String resolutionName) {
    log.info("Received heatmap request for experiment: " + experimentId + ", resolution: " + resolutionName);
    Experiment experiment = experimentService.findExperimentById(experimentId).get();
    Resolution resolution = resolutionService.findResolutionByName(resolutionName).get();
    return heatmapService.getHeatmapByResolution(resolution);
  }

}
