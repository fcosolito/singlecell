package com.lifescs.singlecell.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.ClusterDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dao.model.HeatmapClusterDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.dto.query.TopMarkerDto;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.HeatmapExpression;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.ViolinGroup;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ResolutionService {
  private ResolutionDao resolutionDao;
  private HeatmapClusterDao heatmapClusterDao;
  private ClusterDao clusterDao;
  private CellDao cellDao;
  private GeneExpressionListDao geneExpressionListDao;

  public Optional<Resolution> findResolutionById(String resolutionId) {
    return resolutionDao.findResolutionById(resolutionId);
  }

  public List<Cluster> findClustersByExperiment(Experiment experiment) {
    return clusterDao.findClustersByExperiment(experiment);

  }

  // Should be used with 'addHeatmapClustersForResolution' output
  public void saveHeatmapClusters(List<HeatmapCluster> list) {
    heatmapClusterDao.saveAll(list);
  }

  public void saveResolutions(List<Resolution> resolutions) {
    resolutionDao.saveResolutions(resolutions);
  }

  public void saveClusters(List<Cluster> clusters) {
    clusterDao.saveClusters(clusters);
  }

  // Create ViolinGroups used to draw violin plots
  public List<ViolinGroup> addViolinGroupsForResolution(Experiment e, Resolution r){
    // Create cell to base group map
    // a base group does not set the gene code, so a new one must be created for each 
    // expression list proccessed, for each base group
    Map<ObjectId, ViolinGroup> cellId2BaseGroup = new HashMap<>();
    cellDao.getViolinGroupLoadDtos(e, r).stream().forEach(dto -> {
      ViolinGroup group = new ViolinGroup();
      group.setSampleId(dto.getSample());
      group.setClusterId(dto.getCluster());
      group.setResolutionId(dto.getResolution());
      dto.getCells().stream().forEach(cell -> cellId2BaseGroup.put(cell, group));
    });
    // Get a list of the codes to be loaded
    geneExpressionListDao.findCodesForExperiment(e).stream()
      .forEach(code -> {
        GeneExpressionList gel = geneExpressionListDao.findExpressionListByCode(e, code);
        ViolinGroup baseGroup = cellId2BaseGroup.get();
        ViolinGroup violinGroup = new ViolinGroup();
        violinGroup.setSampleId();
        violinGroup.setClusterId(dto.getCluster());
        violinGroup.setResolutionId(dto.getResolution());
      });
    // for each gene expression, load it, append its expressions to the violin groups, then load the next
  }

  // Create HeatmapClusters used to draw heatmaps
  public List<HeatmapCluster> addHeatmapClustersForResolution(Experiment e, Resolution r, Integer nBuckets,
      Integer markersPerCluster, Boolean useAverage)
      throws NoObjectFoundException, RuntimeException {
    log.info("Creating heatmap clusters for resolution: " + r.getName());
    List<TopMarkerDto> dtos = resolutionDao.getTopMarkers(r, markersPerCluster);

    List<HeatmapCluster> result = new ArrayList<>();
    List<Cluster> clusters = clusterDao.findClustersByResolution(r);

    if (useAverage) {
      for (Cluster c : clusters) {
        result.add(addHeatmapClusterAverage(c, dtos, nBuckets));
      }
    } else {
      for (Cluster c : clusters) {
        result.add(addHeatmapClusterRandom(c, dtos, nBuckets));
      }
    }
    return result;
  }

  private HeatmapCluster addHeatmapClusterRandom(Cluster c, List<TopMarkerDto> markerDtos, Integer nBuckets) {
    log.info("Creating heatmap cluster for cluster: " + c.getName());
    // All the markers for the resolution
    log.info("Marker dtos: " + markerDtos.stream().map(d -> d.getClusterId().toString()).toList().toString());
    List<String> resolutionMarkers = markerDtos.stream()
        .map(d -> d.getTopMarkers()).flatMap(List::stream).toList();

    // Get this cluster's dto
    TopMarkerDto topMarkerDto = markerDtos.stream()
        .filter(d -> d.getClusterId().equals(c.getId())).findFirst()
        .orElseThrow(() -> new RuntimeException("Cluster markers not found for cluster: " + c.getId()));

    // This cluster markers
    List<String> clusterMarkers = topMarkerDto.getTopMarkers();

    HeatmapCluster hc = new HeatmapCluster();
    hc.setCluster(c);
    hc.setTopMarkers(clusterMarkers);

    // This cluster's cell ids
    List<ObjectId> cellIds = new ArrayList<>();
    // The method returns an unmodifiable list
    cellIds.addAll(cellDao.getCellIdsByCluster(c));
    
    // Select nBuckets random cell ids
    List<ObjectId> selectedCellIds = new ArrayList<>();
    Random random = new Random();
    for (int i = 0; i < nBuckets; i++) {
      int randomIndex = random.nextInt(cellIds.size());
      selectedCellIds.add(cellIds.get(randomIndex));
      cellIds.remove(randomIndex);
    }

    // Get marker expressions for the selected cells
    // This could be sped up (maybe) by getting every expression for each selected cell
    // and filtering genes using a map: check if gene code is in map for each expression
    List<HeatmapClusterLoadDto> dtos = cellDao.getMarkerExpressionsByCellIds(selectedCellIds, resolutionMarkers);    
    log.info("Finished loading cluster marker expressions");

    // Fill heatmap cluster
    dtos.stream().forEach(
      dto -> {
        hc.getBuckets().add(dto.getBarcode());
        hc.getExpressions().addAll(dto.getExpressions().stream().map(
          cellExpression -> new HeatmapExpression(dto.getBarcode(), cellExpression.getCode(), cellExpression.getExpression())).toList());
        });
    dtos = null;
    return hc;

  }

  // TODO Change to new model
  private HeatmapCluster addHeatmapClusterAverage(Cluster c, List<TopMarkerDto> markerDtos, Integer nBuckets)
      throws NoObjectFoundException, RuntimeException {
    log.info("Getting heatmap cluster for " + c.getId());

    // All the markers for the resolution
    List<String> resolutionMarkers = markerDtos.stream()
        .map(d -> d.getTopMarkers()).flatMap(List::stream).toList();

    // Get this cluster's dto
    TopMarkerDto topMarkerDto = markerDtos.stream()
        .filter(d -> d.getClusterId().equals(c.getId())).findFirst()
        .orElseThrow(() -> new RuntimeException("Cluster markers not found for cluster: " + c.getId()));

    // This cluster markers
    List<String> clusterMarkers = topMarkerDto.getTopMarkers();

    HeatmapCluster hc = new HeatmapCluster();
    hc.setCluster(c);
    hc.setTopMarkers(clusterMarkers);

    List<HeatmapClusterLoadDto> dtos = clusterDao.getMarkerExpressionsForCluster(c, resolutionMarkers);
    log.info("Finished loading cluster marker expressions");

    Map<String, Integer> bucketSizeMap = new HashMap<>();
    List<String> barcodes = dtos.stream().map(d -> d.getBarcode()).toList();
    Map<String, String> bucketMap = getBuckets(barcodes, nBuckets, bucketSizeMap);

    // Initialize null expressions for each bucket
    List<HeatmapExpression> heatmapExpressions = new ArrayList<>();
    resolutionMarkers.stream()
        .forEach(m -> {
          bucketMap.values().stream().distinct().forEach(
              b -> heatmapExpressions.add(new HeatmapExpression(b, m, 0.0)));
        });
    dtos.stream().forEach(d -> {

      // For each expression object
      d.getExpressions().stream().forEach(dtoExp -> {
        // TODO change this
        Optional<HeatmapExpression> bucketExpOpt = null;
        // Optional<HeatmapExpression> bucketExpOpt = heatmapExpressions.stream()
        // .filter(exp -> exp.getBucketId().equals(bucketMap.get(d.getBarcode())))
        // .filter(exp -> exp.getGeneCode().equals(dtoExp.getGeneCode())).findFirst();
        HeatmapExpression bucketExp;
        if (bucketExpOpt.isPresent())
          bucketExp = bucketExpOpt.get();
        else
          throw new RuntimeException(
              "Failed getting an expression while adding heatmap cluster: " + c.getId());
        // sum expression
        bucketExp.setExpression(bucketExp.getExpression() + dtoExp.getExpression());
      });
    });
    heatmapExpressions.stream()
        .forEach(exp -> exp.setExpression(exp.getExpression() / bucketSizeMap.get(exp.getBucketId())));
    hc.setExpressions(heatmapExpressions);
    hc.setBuckets(bucketMap.values().stream().distinct().toList());

    // TODO change this
    // Add the heatmapCluster to its cluster
    // c.setHeatmapClusterId(hc.getId());

    return hc;

  }

  private Map<String, String> getBuckets(List<String> barcodes, Integer nBuckets, Map<String, Integer> sizes)
      throws RuntimeException {
    log.info("Calculating cluster buckets");
    Map<String, String> result = new HashMap<>();
    Integer bucketLength = barcodes.size() / nBuckets;
    if (bucketLength < 1)
      throw new RuntimeException("Cannot get cluster buckets: bucket size is too small");

    Integer b = 1;
    Integer counter = 0;
    for (String barcode : barcodes) {
      result.put(barcode, Integer.toString(b));
      counter++;
      if (counter >= bucketLength & b < nBuckets) {
        sizes.put(Integer.toString(b), counter);
        b++;
        counter = 0;
      }
    }
    sizes.put(Integer.toString(b), counter);

    return result;

  }

  public List<Resolution> findResolutionsByExperiment(Experiment experiment) {
    return resolutionDao.findResolutionsByExperiment(experiment);
  }

public Optional<Resolution> findResolutionByName(String name) {
    return resolutionDao.findResolutionByName(name);

}
}
