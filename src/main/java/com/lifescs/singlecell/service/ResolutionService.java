package com.lifescs.singlecell.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dao.model.ClusterDao;
import com.lifescs.singlecell.dao.model.HeatmapClusterDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.dto.query.TopMarkerDto;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.HeatmapExpression;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ResolutionService {
  private ResolutionDao resolutionDao;
  private HeatmapClusterDao heatmapClusterDao;
  private ClusterDao clusterDao;

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

  // Create HeatmapClusters used to draw heatmaps
  public List<HeatmapCluster> addHeatmapClustersForResolution(Experiment e, Resolution r, Integer nBuckets,
      Integer markersPerCluster)
      throws NoObjectFoundException, RuntimeException {
    List<TopMarkerDto> dtos = resolutionDao.getTopMarkers(r, markersPerCluster);

    List<HeatmapCluster> result = new ArrayList<>();
    List<Cluster> clusters = clusterDao.findClustersByExperiment(e);

    for (Cluster c : clusters) {
      result.add(addHeatmapCluster(c, dtos, nBuckets));
    }
    return result;
  }

  // TODO Change to new model
  private HeatmapCluster addHeatmapCluster(Cluster c, List<TopMarkerDto> markerDtos, Integer nBuckets)
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

    // TODO Just select nBuckets cells to represent this cluster
    Map<String, Integer> bucketSizeMap = new HashMap<>();
    List<String> barcodes = dtos.stream().map(d -> d.getBarcode()).toList();
    Map<String, String> bucketMap = getBuckets(barcodes, nBuckets, bucketSizeMap);

    // Initialize null expressions for each bucket
    List<HeatmapExpression> heatmapExpressions = new ArrayList<>();
    resolutionMarkers.stream()
        .forEach(m -> {
          bucketMap.values().stream().distinct().forEach(
              b -> heatmapExpressions.add(new HeatmapExpression(b, m)));
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
}
