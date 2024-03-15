package com.lifescs.singlecell.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

@Component
public class PathMapper {

    @Value("${test.inputDirectory}")
    private String projectsBaseDirectory;
    @Value("${test.matrixFile}")
    private String geneExpressionMatrixPath;
    @Value("${test.geneMapFile}")
    private String geneMappingPath;
    @Value("${test.metadataFile}")
    private String metadataPath;
    @Value("${test.markersFile}")
    private String markersPath;
    @Value("${test.matrixCsvFile}")
    private String matrixPath;

    public String subPathOfExperiment(Experiment e) {
        return e.getId() + "/";
    }

    public String pathOfProject(Project p) {
        return projectsBaseDirectory + p.getId() + "/";
    }

    public String pathOfExperiment(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e);
    }

    public String geneExpressionMatrixPath(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e) + geneExpressionMatrixPath;
    }

    public String geneMapPath(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e) + geneMappingPath;
    }

    public String metadataPath(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e) + metadataPath;
    }

    public String markersPath(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e) + markersPath;
    }

    public String matrixPath(Project p, Experiment e) {
        return pathOfProject(p) + subPathOfExperiment(e) + matrixPath;
    }
}
