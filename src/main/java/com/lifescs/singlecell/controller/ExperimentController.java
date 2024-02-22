package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.service.ExperimentInputService;
import com.lifescs.singlecell.service.ExperimentService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ExperimentController {
    private ExperimentInputService experimentInputService;
    private ExperimentService experimentService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/load_example")
    public String loadExample() throws Exception {
        Project p = new Project("Example project");
        p.setId("proj1");
        Experiment e = new Experiment("Example experiment");
        e.setId("exp1");
        p.getExperiments().add(e);

        experimentInputService.loadCellsMetadata(p, e);
        experimentInputService.loadMarkers(p, e);
        experimentService.saveGeneExpressionLists(experimentInputService.loadGeneExpressions(p, e));
        experimentService.saveExperimentDeep(e);

        return "Experiment loaded";

    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/save_marker_expressions")
    public void saveMarkerExpression() throws Exception {
        Experiment e = experimentService.findExperimentById("exp1").get();
        experimentService.saveMarkerExpressionLists(
                experimentService.addMarkerExpressionsForResolution(e, e.getResolutions().get(3)));
        experimentService.saveExperimentDeep(e);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/save_heatmap_clusters")
    public void saveHeatmapClusters() throws Exception {
        Experiment e = experimentService.findExperimentById("exp1").get();
        // experimentService.saveHeatmapClusters(
        experimentService.addHeatmapClustersForResolution(e, e.getResolutions().get(3), 20);
        // experimentService.saveExperimentDeep(e);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/clean")
    public void clean() throws Exception {
        // Clean database
    }

}
