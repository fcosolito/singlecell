package com.lifescs.singlecell.service;

import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.model.HeatmapExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.CellExpressionListDao;
import com.lifescs.singlecell.dao.model.ClusterDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dao.model.HeatmapClusterDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.api.HeatmapClusterLoadDto;
import com.lifescs.singlecell.dto.api.TopMarkerDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
// Class to save and update experiment data
public class ExperimentService {
    private ExperimentDao experimentDao;
    private CellDao cellDao;
    private ResolutionDao resolutionDao;
    private GeneExpressionListDao geneExpressionListDao;
    private ClusterDao clusterDao;
    private HeatmapClusterDao heatmapClusterDao;
    private CellExpressionListDao cellExpressionListDao;

    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperiment(e);
    }

    // Saves the experiment object and the objects related with document references,
    // does not save gene/protein/marker expressions
    public Experiment saveExperimentDeep(Experiment e) {
        return experimentDao.saveExperimentDeep(e);
    }

    public void removeExperiment(String experimentId) {
    }

    // Should be used with 'ExperimentInputService.loadGeneExpressions' output
    public void saveGeneExpressionLists(List<GeneExpressionList> expressionLists) {
        geneExpressionListDao.saveExpressionLists(expressionLists);
    }

    // Should be used with 'ExperimentInputService.loadGeneExpressions' output
    public void saveCellExpressionLists(List<CellExpressionList> expressionLists) {
        cellExpressionListDao.saveExpressionLists(expressionLists);
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

    public Optional<Cell> findCellById(String cellId) {
        return cellDao.findCellById(cellId);
    }

    // move to resolution service TODO
    public Optional<Resolution> findResolutionById(String resolutionId) {
        return resolutionDao.findResolutionById(resolutionId);
    }

    // Should be used with 'addHeatmapClustersForResolution' output
    public void saveHeatmapClusters(List<HeatmapCluster> list) {
        heatmapClusterDao.saveAll(list);
    }

    // Create HeatmapClusters used to draw heatmaps
    public List<HeatmapCluster> addHeatmapClustersForResolution(Experiment e, Resolution r, Integer nBuckets,
            Integer markersPerCluster)
            throws NoObjectFoundException, RuntimeException {
        List<TopMarkerDto> dtos = resolutionDao.getTopMarkers(r, markersPerCluster);

        List<HeatmapCluster> result = new ArrayList<>();
        for (Cluster c : r.getClusters()) {
            result.add(addHeatmapCluster(c, dtos, nBuckets));
        }
        return result;
    }

    private HeatmapCluster addHeatmapCluster(Cluster c, List<TopMarkerDto> markerDtos, Integer nBuckets)
            throws NoObjectFoundException, RuntimeException {
        List<String> resolutionMarkers = markerDtos.stream()
                .map(d -> d.getTopMarkers()).flatMap(List::stream).toList();
        Optional<TopMarkerDto> clusterMarkersOpt = markerDtos.stream()
                .filter(d -> d.getClusterId().equals(c.getId())).findFirst();
        List<String> clusterMarkers;
        if (clusterMarkersOpt.isPresent())
            clusterMarkers = clusterMarkersOpt.get().getTopMarkers();
        else
            throw new RuntimeException("Cluster markers not found for cluster: " + c.getId());
        log.info("Getting heatmap cluster for " + c.getId());
        HeatmapCluster hc = new HeatmapCluster();
        hc.setId(new ObjectId());
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
                            b -> heatmapExpressions.add(new HeatmapExpression(b, m)));
                });
        dtos.stream().forEach(d -> {

            // For each expression object
            d.getExpressions().stream().forEach(dtoExp -> {
                Optional<HeatmapExpression> bucketExpOpt = heatmapExpressions.stream()
                        .filter(exp -> exp.getBucketId().equals(bucketMap.get(d.getBarcode())))
                        .filter(exp -> exp.getGeneCode().equals(dtoExp.getGeneCode())).findFirst();
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

        // Add the heatmapCluster to its cluster
        c.setHeatmapClusterId(hc.getId());

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
}
