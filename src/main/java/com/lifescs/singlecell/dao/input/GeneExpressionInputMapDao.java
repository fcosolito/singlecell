package com.lifescs.singlecell.dao.input;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneExpressionInputMapDao {

    private final GeneExpressionMatrixInputDao matrixInputDao;
    @Value("${test.inputDirectory}") // Change this
    private String projectDirectory;
    @Value("${test.matrixFile}")
    private String matrixPathString;
    @Value("${test.geneMapFile}")
    private String geneMapPathString;

    protected String pathOfExperiment(Experiment e) {
        return projectDirectory + "/" + e.getId() + "/";
    }

    public String generateGeneExpressionKey(Integer cellLocalId, String geneCode) {
        return cellLocalId.toString() + "_" + geneCode.toString();
    }

}
