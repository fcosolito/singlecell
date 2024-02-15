package com.lifescs.singlecell.service;

import com.lifescs.singlecell.model.GeneExpression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.apache.commons.collections4.MultiValuedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.input.CellMetadataInputDao;
import com.lifescs.singlecell.dao.input.GeneExpressionInputMapDao;
import com.lifescs.singlecell.dao.file.GeneExpressionMapDao;
import com.lifescs.singlecell.dao.input.GeneExpressionMatrixInputDao;
import com.lifescs.singlecell.dao.input.MarkerGeneInputDao;
import com.lifescs.singlecell.dao.model.ExperimentDao;
import com.lifescs.singlecell.dto.csv.CellMetadataInputDto;
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.dto.csv.GeneExpressionMatrixInputDto;
import com.lifescs.singlecell.dto.csv.GeneMapDto;
import com.lifescs.singlecell.dto.csv.MarkerGeneInputDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.GeneExpressionMap;
import com.lifescs.singlecell.model.MarkerGene;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;

@Service
public class ExperimentService {
    private GeneExpressionMatrixInputDao matrixDao;
    private CellMetadataInputDao metadataDao;
    private Logger logger;
    private ExperimentDao experimentDao;
    private GeneExpressionInputMapDao inputMapDao;
    private MarkerGeneInputDao markersDao;

    public ExperimentService(GeneExpressionMatrixInputDao matrixDao, CellMetadataInputDao metadataDao,
            ExperimentDao experimentDao, GeneExpressionInputMapDao inputMapDao, MarkerGeneInputDao markersDao) {
        this.matrixDao = matrixDao;
        this.metadataDao = metadataDao;
        this.experimentDao = experimentDao;
        this.inputMapDao = inputMapDao;
        this.markersDao = markersDao;
        this.logger = LoggerFactory.getLogger(ExperimentService.class);

    }

    public Experiment saveExperiment(Experiment e) {
        return experimentDao.saveExperiment(e);
    }

    public Experiment saveExperimentDeep(Experiment e) {
        return experimentDao.saveExperimentDeep(e);
    }

    public void loadGeneExpressions(Project p, Experiment e) throws Exception {
        GeneExpressionMatrixInputDto dto = matrixDao.readMatrix(p, e);
        Map<Integer, String> geneMap = matrixDao.readGeneMapping(p, e);
        Map<Integer, GeneExpressionList> cellMap = e.getCells().stream()
                .collect(Collectors.toMap(c -> c.getLocalId(), c -> c.getGeneExpressions()));
        dto.getGeneExpressionList().stream().forEach(d -> {
            GeneExpression ge = new GeneExpression(geneMap.get(d.getGeneId()), d.getExpression());
            cellMap.get(d.getCellId()).getGeneExpressions().add(ge);
        });

    }

    public void loadCellsFromMetadataFile(Project p, Experiment experiment) throws Exception {
        for (CellMetadataInputDto dto : metadataDao.readCSVToMetadataBeans(p, experiment)) {
            loadCellMetadata(dto, experiment);
        }
    }

    private Cell loadCellMetadata(CellMetadataInputDto dto, Experiment experiment) throws Exception {
        Cell cell = new Cell();
        cell.setId(experiment.getId() + dto.getId().toString());
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
        cell.setSample(experiment.getSamples().stream()
                .filter(s -> s.getName().equals(dto.getSample()))
                .findFirst().orElseGet(() -> addSample(dto.getSample(), experiment)));
        cell.setNumberOfUMIs(dto.getNumberOfUMIs());
        cell.setNumberOfgenes(dto.getNumberOfGenes());
        cell.setPercentOfMitochondrialGenes(dto.getPercentOfMitochondrialGenes());
        for (Entry<String, String> entry : dto.getClusters().entries()) {
            String resolutionName;
            String clusterName;
            Resolution resolution;
            Cluster cluster;
            resolutionName = entry.getKey();
            clusterName = entry.getValue();
            resolution = experiment.getResolutions().stream()
                    .filter(r -> r.getName().equals(resolutionName))
                    .findFirst().orElseGet(() -> addResolution(resolutionName, experiment));
            cluster = resolution.getClusters().stream()
                    .filter(c -> c.getName().equals(clusterName))
                    .findFirst().orElseGet(() -> addCluster(clusterName, resolution));
            cluster.setResolution(resolution);
            List<Cluster> cs = cell.getClusters();
            cs.add(cluster);
            cell.setClusters(cs);

        }

        experiment.getCells().add(cell);
        logger.info("Cell " + cell.getBarcode() + " loaded to experiment " + experiment.getName() + "sample: "
                + cell.getSample().getName());
        return cell;

    }

    public void loadMarkersFromFile(Project p, Experiment e) throws Exception {
        logger.info("Loading marker genes from file");
        for (MarkerGeneInputDto dto : markersDao.readCSVToMetadataBeans(p, e)) {
            loadMarkerGene(dto, p, e);
        }
    }

    private MarkerGene loadMarkerGene(MarkerGeneInputDto dto, Project p, Experiment experiment)
            throws RuntimeException {
        MarkerGene marker = new MarkerGene();
        marker.setGeneCode(dto.getGeneCode());
        marker.setFoldChange(dto.getFoldChange());
        marker.setPValue(dto.getPValue());
        marker.setAdjacentPValue(dto.getAdjacentPValue());
        marker.setPercent1(dto.getPercent1());
        marker.setPercent2(dto.getPercent2());
        String formatedResolutionId = "cluster_" + String.format("%.2f", Double.parseDouble(dto.getResolution()));
        Optional<Cluster> opCluster = experiment.getResolutions().stream()
                .map(r -> r.getClusters()).flatMap(List::stream)
                .filter(c -> c.getId()
                        .equals((experiment.getId() + formatedResolutionId + dto.getCluster()))) // Clean hardcoded
                                                                                                 // resolution name
                .findFirst();
        if (opCluster.isPresent()) {
            opCluster.get().getMarkers().add(marker);
        } else {
            throw new RuntimeException("Cluster not found: " + formatedResolutionId + dto.getCluster());
        }
        return marker;

    }

    public Optional<Cluster> findClusterById(String id) {
        return experimentDao.findClusterById(id);
    }

    public List<Double> findExpressions(Integer cellLocalId, List<String> genes, Experiment e) throws Exception {
        return inputMapDao.findExpressions(cellLocalId, genes, e);
    }

    private Sample addSample(String name, Experiment experiment) {
        Sample s = new Sample(name);
        experiment.getSamples().add(s);
        return s;

    }

    private Resolution addResolution(String name, Experiment experiment) {
        Resolution r = new Resolution(name, experiment);
        experiment.getResolutions().add(r);
        return r;
    }

    private Cluster addCluster(String name, Resolution resolution) {
        Cluster c = new Cluster(name, resolution);
        resolution.getClusters().add(c);
        return c;
    }

    public Optional<Experiment> findExperimentById(String string) {
        return experimentDao.findExperimentById(string);
    }

}
