package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.LowDimensionalService;
import com.lifescs.singlecell.service.ResolutionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class LowDimensionalController {
  private LowDimensionalService lowDimensionalService;
  private ExperimentService experimentService;
  private ResolutionService resolutionService;

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/lowDimensional/byGenes")
  public LowDimensionalDto getDtoByGenes(@PathVariable(name = "id") String experimentId) {
    Experiment e = experimentService.findExperimentById(experimentId).get();
    List<String> geneCodes = new ArrayList<>();
    geneCodes.add("Naaa");
    geneCodes.add("Vcpip1");
    geneCodes.add("Tram1");

    return lowDimensionalService.getLowDimensionalByGene(e, geneCodes);
  }

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/lowDimensional/byResolution")
  public LowDimensionalDto getDtoByResolution(@PathVariable(name = "id") String experimentId, @RequestParam String resolution) {
    Experiment e = experimentService.findExperimentById(experimentId).get();
    Resolution r = resolutionService.findResolutionByName(resolution).get();

    return lowDimensionalService.getLowDimensionalByResolution(e, r);
  }

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/lowDimensional/bySamples")
  public LowDimensionalDto getDtoBySamples(@PathVariable(name = "id") String experimentId) {
    Experiment e = experimentService.findExperimentById(experimentId).get();

    return lowDimensionalService.getLowDimensionalBySamples(e);
  }
}
