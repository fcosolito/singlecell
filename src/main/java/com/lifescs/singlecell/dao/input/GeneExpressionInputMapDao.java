package com.lifescs.singlecell.dao.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.csv.GeneExpressionMatrixInputDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionMap;

@Component
public class GeneExpressionInputMapDao {

    private GeneExpressionMatrixInputDao matrixInputDao;
    private Logger logger;
    @Value("${test.inputDirectory}") // Change this
    private String projectDirectory;
    @Value("${test.matrixFile}")
    private String matrixPathString;
    @Value("${test.geneMapFile}")
    private String geneMapPathString;

    public GeneExpressionInputMapDao(GeneExpressionMatrixInputDao matrixInputDao) {
        this.matrixInputDao = matrixInputDao;
        this.logger = LoggerFactory.getLogger(GeneExpressionInputMapDao.class);
    }

    protected String pathOfExperiment(Experiment e) {
        return projectDirectory + "/" + e.getId() + "/";
    }

    public String generateGeneExpressionKey(Integer cellLocalId, String geneCode) {
        return cellLocalId.toString() + "_" + geneCode.toString();
    }

    public GeneExpressionMap loadMapFromMatrix(Experiment e) throws Exception {
        Map<Integer, String> geneMap = matrixInputDao.readGeneMapping(pathOfExperiment(e) + geneMapPathString);
        GeneExpressionMatrixInputDto dto = matrixInputDao.readMatrix(pathOfExperiment(e) + matrixPathString);
        GeneExpressionMap gep = new GeneExpressionMap();

        logger.info("Generating gene expression map from input matrix");
        gep.setMap(dto.getGeneExpressionList().stream().collect(Collectors.toMap(
                ge -> generateGeneExpressionKey(ge.getCellId(), geneMap.get(ge.getGeneId())),
                ge -> ge.getExpression())));
        return gep;
    }

    public List<Double> findExpressions(Integer cellLocalId, List<String> genes, Experiment e) throws Exception {
        List<Double> expressions = new ArrayList<>();
        GeneExpressionMap map;
        if (false) { // experiment has a loaded map
            // get loaded map
        } else {
            map = loadMapFromMatrix(e);
        }
        for (String g : genes) {
            expressions.add(map.getMap().get(generateGeneExpressionKey(cellLocalId, g)));
        }
        return expressions;
    }
}
