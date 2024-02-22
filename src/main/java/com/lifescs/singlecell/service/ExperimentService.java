package com.lifescs.singlecell.service;

import com.lifescs.singlecell.model.GeneExpression;
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
import com.lifescs.singlecell.dao.model.ClusterDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dao.model.HeatmapClusterDao;
import com.lifescs.singlecell.dao.model.MarkerExpressionListDao;
import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.model.HeatmapClusterLoadDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.MarkerExpressionList;
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
    private MarkerExpressionListDao markerExpressionListDao;
    private ClusterDao clusterDao;
    private HeatmapClusterDao heatmapClusterDao;

    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperiment(e);
    }

    // Saves the experiment object and the objects related with document references,
    // does not save gene/protein/marker expressions
    public Experiment saveExperimentDeep(Experiment e) {
        return experimentDao.saveExperimentDeep(e);
    }

    // Returns a list of the marker expression containers for each cell
    // Also adds the container ids to the cells
    // IMPORTANT: Either both updated cells and the container must be saved or none,
    // otherwise the database will turn inconsistent
    public List<MarkerExpressionList> addMarkerExpressionsForResolution(Experiment e, Resolution r) {
        List<Cell> cells = e.getCells();
        List<MarkerExpressionList> expressionList = new ArrayList<>();

        long start = System.nanoTime();

        // Try with parallel stream ?
        for (Cell c : cells) {
            expressionList.add(addCellMarkerExpressions(c, r));
        }

        long end = System.nanoTime();
        long elapsedTime = end - start;
        log.info("Finished setting expressions: " + elapsedTime + " s");

        return expressionList;
    }

    // Should be used with 'addMarkerExpressionForResolution' output
    public void saveMarkerExpressionLists(List<MarkerExpressionList> expressionLists) {
        log.info(expressionLists.get(0).getMarkerExpressions().toString());
        markerExpressionListDao.saveExpressionLists(expressionLists);
    }

    // Should be used with 'ExperimentInputService.loadGeneExpressions' output
    public void saveGeneExpressionLists(List<GeneExpressionList> expressionLists) {
        geneExpressionListDao.saveExpressionLists(expressionLists);
    }

    // Should be called only from the public method
    // 'addMarkerExpressionsForResolution'
    private MarkerExpressionList addCellMarkerExpressions(Cell c, Resolution r) {
        // Get list of gene expressions
        List<GeneExpression> markerExpressions = cellDao.getMarkerExpressionsForResolution(c, r);
        if (markerExpressions.isEmpty())
            log.info("It IS null");

        // Create container class MarkerExpressionList
        MarkerExpressionList mel = new MarkerExpressionList();
        mel.setMarkerExpressions(markerExpressions);
        mel.setResolution(r);
        mel.setId(new ObjectId());

        // Add container id to its cell
        c.getMarkerExpressionIds().add(mel.getId());
        return mel;
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

    public Optional<Cell> findCellById(String cellId) {
        return cellDao.findCellById(cellId);
    }

    public Optional<Resolution> findResolutionById(String resolutionId) {
        return resolutionDao.findResolutionById(resolutionId);
    }

    // Should be used with 'addHeatmapClustersForResolution' output
    public void saveHeatmapClusters(List<HeatmapCluster> list) {
        heatmapClusterDao.saveAll(list);
    }

    // Create HeatmapClusters used to draw heatmaps
    public List<HeatmapCluster> addHeatmapClustersForResolution(Experiment e, Resolution r, Integer nBuckets)
            throws NoObjectFoundException, RuntimeException {
        List<HeatmapCluster> result = new ArrayList<>();
        for (Cluster c : r.getClusters()) {
            result.add(addHeatmapCluster(e, c, nBuckets));
        }
        return result;
    }

    private HeatmapCluster addHeatmapCluster(Experiment e, Cluster c, Integer nBuckets)
            throws NoObjectFoundException, RuntimeException {
        List<HeatmapClusterLoadDto> dtos = clusterDao.getMarkerExpressionsForCluster(e, c);
        HeatmapCluster hc = new HeatmapCluster();
        hc.setId(new ObjectId());
        List<String> barcodes = dtos.stream().map(d -> d.getBarcode()).toList();
        Map<String, Integer> bucketSizeMap = new HashMap<>();
        Map<String, String> bucketMap = getBuckets(barcodes, nBuckets, bucketSizeMap);
        log.info(bucketMap.toString());
        log.info("Sizes\n\n\n");
        log.info(bucketSizeMap.toString());
        List<HeatmapExpression> expressions = new ArrayList<>();

        // For each cell dto
        dtos.stream().forEach(d -> {

            log.info("Cluster: " + c.getId() + " Size: " + d.getExpressions().size());
            // For each expression object
            d.getExpressions().stream().forEach(dtoExp -> {
                HeatmapExpression expr = expressions.stream()
                        .filter(exp -> exp.getBucketId().equals(bucketMap.get(d.getBarcode())))
                        .filter(exp -> exp.getGeneCode().equals(dtoExp.getGeneCode())).findFirst().orElseGet(() -> {
                            HeatmapExpression exprAux = new HeatmapExpression(bucketMap.get(d.getBarcode()),
                                    dtoExp.getGeneCode());
                            expressions.add(exprAux);
                            return exprAux;
                        });
                // sum expression or add new one
                expr.setExpression(expr.getExpression() + dtoExp.getExpression());
            });
        });
        log.info("Finished adding expressions");
        expressions.stream()
                .forEach(exp -> exp.setExpression(exp.getExpression() / bucketSizeMap.get(exp.getBucketId())));
        hc.setExpressions(expressions);
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
