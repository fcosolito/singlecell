package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.LowDimensionalService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class LowDimensionalController {
  private LowDimensionalService lowDimensionalService;
  private ExperimentService experimentService;

  @GetMapping("/experiment/{id}/plots/lowDimensional")
  public LowDimensionalDto getClustersByExperiment(@PathVariable(name = "id") String experimentId, @RequestParam Boolean byGene) {
    Experiment e = experimentService.findExperimentById(experimentId).get();
    LowDimensionalDto result = null;
    List<String> geneCodes = new ArrayList<>();
    geneCodes.add("Naaa");
    geneCodes.add("Vcpip1");
    geneCodes.add("Tram1");
    if (byGene)
      result = lowDimensionalService.getLowDimensionalByGene(e, geneCodes);
    return result;
  }

}
