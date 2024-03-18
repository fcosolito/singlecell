package com.lifescs.singlecell.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

@Component
public class PathMapper {

    @Value("${singlecell.path.InputDirectory}")
    private String inputDirectory;
    @Value("${singlecell.path.MetadataFile}")
    private String metadataPath;
    @Value("${singlecell.path.MarkersFile}")
    private String markersPath;
    @Value("${singlecell.path.CellMatrixFile}")
    private String cellMatrixPath;
    @Value("${singlecell.path.GeneMatrixFile}")
    private String geneMatrixPath;

    public String experimentSubPath(Experiment e) {
        return e.getId() + "/";
    }

    public String projectPath(Project p) {
        return inputDirectory + p.getId() + "/";
    }

    public String experimentPath(Project p, Experiment e) {
        return projectPath(p) + experimentSubPath(e);
    }

    public String metadataPath(Project p, Experiment e) {
        return projectPath(p) + experimentSubPath(e) + metadataPath;
    }

    public String markersPath(Project p, Experiment e) {
        return projectPath(p) + experimentSubPath(e) + markersPath;
    }

    public String cellMatrixPath(Project p, Experiment e) {
        return projectPath(p) + experimentSubPath(e) + cellMatrixPath;
    }

    public String geneMatrixPath(Project p, Experiment e) {
        return projectPath(p) + experimentSubPath(e) + geneMatrixPath;
    }
}
