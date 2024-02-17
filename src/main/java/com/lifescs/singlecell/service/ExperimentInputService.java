package com.lifescs.singlecell.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.input.CellMetadataInputDao;
import com.lifescs.singlecell.dao.input.GeneExpressionMatrixInputDao;
import com.lifescs.singlecell.dao.input.MarkerGeneInputDao;
import com.lifescs.singlecell.dto.csv.CellMetadataInputDto;
import com.lifescs.singlecell.dto.csv.GeneExpressionMatrixInputDto;
import com.lifescs.singlecell.dto.csv.MarkerGeneInputDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.MarkerGene;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
// Class for loading experiment data from files
public class ExperimentInputService {
    private GeneExpressionMatrixInputDao matrixDao;
    private CellMetadataInputDao metadataDao;
    private MarkerGeneInputDao markersDao;

    // Loads gene expressions for each cell in the experiment
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

    // Loads cell objects into an experiment
    public void loadCellsMetadata(Project p, Experiment experiment) throws Exception {
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
        log.info("Cell " + cell.getBarcode() + " loaded to experiment " + experiment.getName() + "sample: "
                + cell.getSample().getName());
        return cell;

    }

    // Loads marker genes into the clusters of the experiment
    public void loadMarkers(Project p, Experiment e) throws Exception {
        log.info("Loading marker genes from file");
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

}
