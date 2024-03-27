package com.lifescs.singlecell.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.api.ClusterTreeDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.service.ClusterTreeService;
import com.lifescs.singlecell.service.ExperimentService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ClusterTreeController {
  private ClusterTreeService clusterTreeService;
  private ExperimentService experimentService;

  @CrossOrigin(origins = "http://localhost:3000")
  @GetMapping("/experiment/{id}/plots/clusterTree")
  public List<ClusterTreeDto> getDtoByResolution(@PathVariable(name = "id") String experimentId) {
    log.info("Received cluster tree request for experiment: " + experimentId);
    Experiment experiment = experimentService.findExperimentById(experimentId).get();
    return clusterTreeService.getDtos(experiment);
  }
}
