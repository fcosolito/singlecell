package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.ResolutionService;
import com.lifescs.singlecell.service.ViolinService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ViolinController {
  private ViolinService violinService;
  private ResolutionService resolutionService;
  private ExperimentService experimentService;

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/violin/byResolution")
  public List<ViolinDto> getDtoByResolution(@PathVariable(name = "id") String experimentId, 
    @RequestParam(name = "res") String resolutionName,
    @RequestParam(name = "genes") List<String> codes) {
    log.info("Received violin plot request for experiment: " + experimentId + ", resolution: " + resolutionName);
    Experiment experiment = experimentService.findExperimentById(experimentId).get();
    Resolution resolution = resolutionService.findResolutionByName(resolutionName).get();
    return violinService.getDtosByResolution(experiment, codes, resolution);
  }

}
