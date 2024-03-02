package com.lifescs.singlecell.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
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
        e.setId("exp3");
        p.getExperiments().add(e);
        // Experiment e = experimentService.findExperimentById("exp1").get();

        experimentInputService.loadCellsMetadata(p, e);
        experimentInputService.loadMarkers(p, e);
        long start = System.nanoTime();
        // experimentService.saveGeneExpressionLists(experimentInputService.loadGeneExpressions(p,
        // e));
        // experimentService.saveCellExpressionLists(experimentInputService.loadCellExpressions(p,
        // e));
        experimentInputService.loadAndSaveExpressions(p, e);
        long end = System.nanoTime();
        log.info("Elapsed time: " + (end - start) / 1_000_000_000.0 + " seconds");
        experimentService.saveExperimentDeep(e);

        return "Experiment loaded";

    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/save_heatmap_clusters")
    public void saveHeatmapClusters() throws Exception {
        Experiment e = experimentService.findExperimentById("exp1").get();
        List<HeatmapCluster> list = new ArrayList<>();
        for (Resolution r : e.getResolutions()) {
            list.addAll(experimentService.addHeatmapClustersForResolution(e, r, 20, 20));
        }
        experimentService.saveHeatmapClusters(list);
        experimentService.saveExperimentDeep(e);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment/clean")
    public void clean() throws Exception {
        // Clean database
    }

}
