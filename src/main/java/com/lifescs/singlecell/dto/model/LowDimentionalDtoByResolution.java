package com.lifescs.singlecell.dto.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LowDimentionalDtoByResolution {

    private String barcodes;
    private List<Double> spring1;
    private List<Double> spring2;
    private List<Double> pca1;
    private List<Double> pca2;
    private List<Double> umap1;
    private List<Double> umap2;
    private List<Double> tsne1;
    private List<Double> tsne2;
    private List<String> samples;
    private List<List<ResolutionDto>> resolutions;

}
