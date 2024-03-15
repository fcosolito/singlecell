package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dto.input.LoadedMetadataDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.service.ExperimentInputService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class ExperimentInputController {
    private ExperimentInputService experimentInputService;

    @CrossOrigin
    @GetMapping("/experiment/{id}/load")
    public String loadExperiment(@PathVariable String id) throws Exception {
        Project p = new Project("Example project");
        p.setId("proj1");
        Experiment e = new Experiment("Example experiment");
        e.setId(id);
        e.setProject(p);

        long start = System.nanoTime();
        LoadedMetadataDto loadedMetadataDto = experimentInputService.loadCellsMetadata(p, e);
        experimentInputService.loadMarkers(p, e, loadedMetadataDto.getClusters());
        experimentInputService.saveLoadedExperiment(e, loadedMetadataDto);
        experimentInputService.loadAndSaveExpressions(p, e);
        long end = System.nanoTime();
        log.info("Elapsed time: " + (end - start) / 1_000_000_000.0 + " seconds");

        return "Experiment loaded";

    }

}
