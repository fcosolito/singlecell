package com.lifescs.singlecell.controller;

import com.lifescs.singlecell.dao.input.CellMetadataInputDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dao.model.ExperimentTestDao;
import com.lifescs.singlecell.dto.csv.CellMetadataInputDto;
import com.lifescs.singlecell.dto.model.ExperimentTestDto;
import com.lifescs.singlecell.dto.model.LowDimentionalDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionMap;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;
import com.lifescs.singlecell.service.ExperimentService;

import lombok.AllArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestController {

    @Value("${test.inputDirectory}")
    private String inputDir;
    private ExperimentService experimentService;
    private Logger logger = LoggerFactory.getLogger(TestController.class);

    public TestController(ExperimentService experimentService) {
        this.experimentService = experimentService;
        this.logger = LoggerFactory.getLogger(TestController.class);
    }

    @GetMapping("/test")
    public String test(Model model) throws Exception {

        Project project = new Project();
        Experiment experiment = new Experiment("Prueba 1");
        experiment.setId("exp1");
        project.setId("proj1");
        project.getExperiments().add(experiment);

        // Read from input files
        long mid = System.nanoTime();
        List<String> searchList = new ArrayList<>();
        searchList.add("Tram1");
        searchList.add("Vcpip1");
        // List<LowDimentionalDto> lowDto = testDao.getLowDimentionalDto(experiment,
        // searchList);
        LowDimentionalDto lowDto = testDao.performAggregation(experiment.getId(), searchList);
        logger.info(lowDto.getCellIds().toString());
        // experimentService.loadCellsFromMetadataFile(project, experiment);
        // experimentService.loadMarkersFromFile(project, experiment);
        // experimentService.loadGeneExpressions(project, experiment);
        // Save to database (recursive)
        // experimentService.saveExperimentDeep(experiment);
        // long start = System.nanoTime();
        // experiment = experimentService.findExperimentById("exp1").get();
        // List<ExperimentTestDto> dtos = testDao.getDto();
        // List<CellDto> cells = new ArrayList<>();
        // for (int i = 0; i < dtos.get(0).getBarcodes().size() - 1; i++) {
        // cells.add(new CellDto(dtos.get(0).getBarcodes().get(i),
        // dtos.get(0).getUmis().get(i)));
        // }
        // model.addAttribute("cells", cells);
        // model.addAttribute("experimentId", dtos.get(0).getExperimentId());
        // logger.info(dtos.get(0).getExperimentId());
        // experimentService.loadExpressionMap(experiment);
        long end = System.nanoTime();
        // logger.info("Time elapsed: " + (mid - start) / 1_000_000_000.0 + " seconds");
        logger.info("Time elapsed: " + (end - mid) / 1_000_000_000.0 + " seconds");
        // logger.info(experiment.getResolutions().toString());
        // experimentService.saveGeneExpressionMap(null, experiment);

        return "TestView";
    }

    // Origins from where the requests are made
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/experiment")
    public ExperimentTestDto getExperiment() {
        List<ExperimentTestDto> dtos = testDao.getDto();
        logger.info("Experiment requested");
        return dtos.get(0);
    }

}
