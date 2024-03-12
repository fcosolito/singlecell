package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dao.model.PlotDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.service.ExperimentService;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@AllArgsConstructor
public class DaoTestController {
    private ExperimentService experimentService;
    private PlotDao plotDao;

    /*
     * @GetMapping("/test/geneCodes2")
     * public List<ViolinDto> getMethodName() {
     * Experiment e = experimentService.findExperimentById("exp1").get();
     * List<String> searchList = new ArrayList<>();
     * searchList.add("Vcpip1");
     * searchList.add("Tram1");
     * searchList.add("Naaa");
     * searchList.add("Tubab1");
     * return plotDao.getViolinDtos(e, e.getResolutions().get(3), searchList);
     * }
     */

}
