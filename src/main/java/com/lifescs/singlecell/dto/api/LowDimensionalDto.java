package com.lifescs.singlecell.dto.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LowDimensionalDto {
    private List<String> barcodes;
    private List<Double> spring1;
    private List<Double> spring2;
    private List<Double> pca1;
    private List<Double> pca2;
    private List<Double> umap1;
    private List<Double> umap2;
    private List<Double> tsne1;
    private List<Double> tsne2;
    private List<String> samples;
    private List<String> clusterNames;
    private List<Double> expressionSum;

    public LowDimensionalDto() {
        this.barcodes = new ArrayList<>();
        this.spring1 = new ArrayList<>();
        this.spring2 = new ArrayList<>();
        this.pca1 = new ArrayList<>();
        this.pca2 = new ArrayList<>();
        this.umap1 = new ArrayList<>();
        this.umap2 = new ArrayList<>();
        this.tsne1 = new ArrayList<>();
        this.tsne2 = new ArrayList<>();
        this.samples = new ArrayList<>();
        this.clusterNames = new ArrayList<>();
        this.expressionSum = new ArrayList<>();
    }

}
