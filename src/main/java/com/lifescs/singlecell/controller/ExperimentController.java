package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.service.ExperimentService;
import com.lifescs.singlecell.service.ResolutionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ExperimentController {
  private ExperimentService experimentService;
  private ResolutionService resolutionService;

  @CrossOrigin
  @GetMapping("/experiment/{id}/saveHeatmapClusters")
  public void saveHeatmapClusters(@PathVariable(name = "id") String experimentId) throws Exception {
    Experiment e = experimentService.findExperimentById(experimentId).get();
    resolutionService.findResolutionsByExperiment(e).stream()
        .forEach(resolution -> resolutionService
            .saveHeatmapClusters(resolutionService.addHeatmapClustersForResolution(e, resolution, 20, 20, false)));
  }

  @CrossOrigin
  @GetMapping("/experiment/{id}/delete")
  public void deleteExperiment(@PathVariable String id) {
    Experiment aux = new Experiment(id);
    aux.setId(id);
    experimentService.deleteExperiment(aux);
  }

  @CrossOrigin
  @GetMapping("/experiment/clean")
  public void cleanDeletedExperiments() {
    experimentService.cleanDeletedExperiments();
  }

}
