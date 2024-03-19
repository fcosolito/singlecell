package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.service.ExperimentService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ExperimentController {
    private ExperimentService experimentService;

    /*
     * @CrossOrigin
     * 
     * @GetMapping("/experiment/save_heatmap_clusters")
     * public void saveHeatmapClusters() throws Exception {
     * Experiment e = experimentService.findExperimentById("exp1").get();
     * List<HeatmapCluster> list = new ArrayList<>();
     * for (Resolution r : e.getResolutions()) {
     * list.addAll(experimentService.addHeatmapClustersForResolution(e, r, 20, 20));
     * }
     * experimentService.saveHeatmapClusters(list);
     * experimentService.saveExperimentDeep(e);
     * }
     */

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
