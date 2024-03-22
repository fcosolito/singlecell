package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dao.input.ExpressionInputDao;
import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.ClusterDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.dto.input.CellExpressionDto;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.service.ExperimentService;
import com.opencsv.bean.CsvToBean;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@AllArgsConstructor
@Slf4j
public class DaoTestController {
  private ExperimentService experimentService;
  private ResolutionDao resolutionDao;
  private ClusterDao clusterDao;
  private CellDao cellDao;
  private ExpressionInputDao expressionInputDao;

  @GetMapping("/test/findResolutionsByExperiment")
  public List<Resolution> getResolutionsByExperiment(@RequestParam String experiment) {
    Experiment e = experimentService.findExperimentById(experiment).get();
    return resolutionDao.findResolutionsByExperiment(e);
  }

  @GetMapping("/test/findClustersByExperiment")
  public List<Cluster> getClustersByExperiment(@RequestParam String experiment) {
    Experiment e = experimentService.findExperimentById(experiment).get();
    return clusterDao.findClustersByExperiment(e);
  }

  @GetMapping("/test/getCellIds")
  public String getCellIdsByCluster() {
    Experiment e = experimentService.findExperimentById("exp1").get();
    Cluster cluster = clusterDao.findClustersByExperiment(e).get(5);
    return cellDao.getCellIdsByCluster(cluster).toString();
  }

  @GetMapping("/test/expressionsByCellIds")
  public List<HeatmapClusterLoadDto> getExpressionsByCellIds() {
    Experiment e = experimentService.findExperimentById("exp1").get();
    Cluster cluster = clusterDao.findClustersByExperiment(e).get(5);
    List<ObjectId> idList = cellDao.getCellIdsByCluster(cluster);
    List<String> geneCodes = new ArrayList<>();
    geneCodes.add("Vcpip1");
    geneCodes.add("Naaa");

    return cellDao.getMarkerExpressionsByCellIds(idList, geneCodes);
  }

  @GetMapping("/test/findCellsByExperiment")
  public String getCellsByExperiment(@RequestParam String experiment) {
    Experiment e = experimentService.findExperimentById(experiment).get();
    long start = System.nanoTime();
    cellDao.findCellsByExperiment(e);
    long end = System.nanoTime();
    return "Time to find cells: " + (end - start) / 1_000_000_000.0 + " s";
  }

  @GetMapping("/test/expressionInput")
  public String testExpressionInput() throws IOException {
    Project p = new Project("proj1");
    Experiment e = new Experiment("exp1");
    e.setId("exp1");

    expressionInputDao.loadCellExpressions(p, e);

    return "Test expression input";
  }
}
