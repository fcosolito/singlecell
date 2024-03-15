package com.lifescs.singlecell.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.input.CellMetadataInputDao;
import com.lifescs.singlecell.dao.input.GeneExpressionMatrixInputDao;
import com.lifescs.singlecell.dao.input.MarkerGeneInputDao;
import com.lifescs.singlecell.dao.model.CellExpressionListDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dto.input.CellMetadataInputDto;
import com.lifescs.singlecell.dto.input.LoadedMetadataDto;
import com.lifescs.singlecell.dto.input.MarkerGeneInputDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellCluster;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.MarkerGene;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// How to load experiment data:
// Files must be stored inside a common directory named after the experiment id,
// inside a directory
// named after the project id
// 1. Create a Project and an Experiment inside it
// 2. Load cell metadata (This will also create resolutions, clusters and
// samples)
// 3. Load markers
// 4. Load gene expressions
// 5. Fill expression lists: Merge all the partial expression lists
// The loaded data can be saved to database between steps

// Class for loading experiment data from files
@Service
@AllArgsConstructor
@Slf4j
public class ExperimentInputService {
    private GeneExpressionMatrixInputDao matrixDao;
    private CellMetadataInputDao metadataDao;
    private MarkerGeneInputDao markersDao;
    private ResolutionService resolutionService;
    private ExperimentService experimentService;
    private GeneExpressionListDao geneExpressionListDao;
    private CellExpressionListDao cellExpressionListDao;

    public void loadAndSaveExpressions(Project p, Experiment e) throws Exception {
        matrixDao.readMatrix(p, e, 500000L);
    }

    public void saveLoadedExperiment(Experiment e, LoadedMetadataDto dto) {
        // Consider ids are created by mongo when ordering saves
        log.info("Saving loaded experiment");
        experimentService.saveExperiment(e);

        log.info("Saving loaded samples");
        experimentService.saveSamples(dto.getSamples());

        log.info("Saving loaded resolutions");
        resolutionService.saveResolutions(dto.getResolutions());

        log.info("Saving loaded clusters");
        resolutionService.saveClusters(dto.getClusters());

        log.info("Saving loaded cells");
        experimentService.saveCells(dto.getCells());

    }

    public void fillExpressionLists(Experiment e) {
        cellExpressionListDao.fillExpressionLists();
        geneExpressionListDao.fillExpressionLists();
    }

    // Loads cell objects into an experiment
    public LoadedMetadataDto loadCellsMetadata(Project p, Experiment experiment) throws Exception {
        log.info("Loading cells for experiment: " + experiment.getName());
        experiment.setCells(new ArrayList<>());
        LoadedMetadataDto loadedDto = new LoadedMetadataDto();
        for (CellMetadataInputDto dto : metadataDao.readCSVToMetadataBeans(p, experiment)) {
            loadedDto.getCells().add(loadCellMetadata(dto, experiment, loadedDto.getSamples(),
                    loadedDto.getResolutions(), loadedDto.getClusters()));
        }
        return loadedDto;
    }

    private Cell loadCellMetadata(CellMetadataInputDto dto, Experiment experiment, List<Sample> samples,
            List<Resolution> resolutions, List<Cluster> clusters) throws Exception {
        Cell cell = new Cell();
        cell.setExperiment(experiment);
        cell.setLocalId(dto.getId());
        cell.setCellNameHigh(dto.getCellNameHigh());
        cell.setCellNameLow(dto.getCellNameLow());
        cell.setBarcode(dto.getBarcode());
        cell.setSpring1(dto.getSpring1());
        cell.setSpring2(dto.getSpring2());
        cell.setPca1(dto.getPca1());
        cell.setPca2(dto.getPca2());
        cell.setUmap1(dto.getUmap1());
        cell.setUmap2(dto.getUmap2());
        cell.setTsne1(dto.getTsne1());
        cell.setTsne2(dto.getTsne2());
        cell.setSample(samples.stream()
                .filter(s -> s.getName().equals(dto.getSample()))
                .findFirst().orElseGet(() -> {
                    Sample newSample = new Sample(dto.getSample());
                    newSample.setExperiment(experiment);
                    samples.add(newSample);
                    return newSample;
                }));
        cell.setNumberOfUMIs(dto.getNumberOfUMIs());
        cell.setNumberOfgenes(dto.getNumberOfGenes());
        cell.setPercentOfMitochondrialGenes(dto.getPercentOfMitochondrialGenes());

        // Set cell clusters
        List<CellCluster> cs = new ArrayList<>();
        for (Entry<String, String> entry : dto.getClusters().entries()) {
            String resolutionName;
            String clusterName;
            Resolution resolution;
            Cluster cluster;
            resolutionName = entry.getKey();
            clusterName = entry.getValue();
            resolution = resolutions.stream()
                    .filter(r -> r.getName().equals(resolutionName))
                    .findFirst().orElseGet(() -> {
                        Resolution newResolution = new Resolution(resolutionName, experiment);
                        resolutions.add(newResolution);
                        return newResolution;
                    });
            cluster = clusters.stream()
                    .filter(c -> c.getName().equals(clusterName)
                            & c.getResolution().getName().equals(resolution.getName()))
                    .findFirst().orElseGet(() -> {
                        Cluster newCluster = new Cluster(clusterName, resolution);
                        newCluster.setResolution(resolution);
                        clusters.add(newCluster);
                        return newCluster;
                    });

            CellCluster cc = new CellCluster();
            cc.setCluster(cluster);
            cc.setResolution(resolution);
            cs.add(cc);
        }
        cell.setCellClusters(cs);
        experiment.getCells().add(cell);

        return cell;

    }

    // Loads marker genes into the clusters of the experiment
    public List<Cluster> loadMarkers(Project p, Experiment e, List<Cluster> clusters) throws Exception {
        log.info("Loading marker genes from file");
        for (MarkerGeneInputDto dto : markersDao.readCSVToMetadataBeans(p, e)) {
            loadMarkerGene(dto, clusters);
        }
        return clusters;
    }

    private MarkerGene loadMarkerGene(MarkerGeneInputDto dto, List<Cluster> clusters)
            throws RuntimeException { // TODO add custom exceptions
        MarkerGene marker = new MarkerGene();
        marker.setGeneCode(dto.getGeneCode());
        marker.setFoldChange(dto.getFoldChange());
        marker.setPValue(dto.getPValue());
        marker.setAdjacentPValue(dto.getAdjacentPValue());
        marker.setPercent1(dto.getPercent1());
        marker.setPercent2(dto.getPercent2());
        Cluster cluster = clusters.stream()
                .filter(c -> c.getName().equals(dto.getCluster())
                        & c.getResolution().getName().equals(dto.getResolution()))
                .findFirst().orElseThrow(() -> new RuntimeException("Cluster not found: " + dto.getResolution() + " " +
                        dto.getCluster()));

        cluster.getMarkers().add(marker);
        return marker;

    }

}
