package com.lifescs.singlecell.dto.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LowDimentionalDto {
    List<String> cellIds;
    List<Double> spring1;
    List<Double> spring2;
    // List<Double> pca1;
    // List<Double> pca2;
    // List<Double> umap1;
    // List<Double> umap2;
    List<Double> tsne1;
    List<Double> tsne2;
    // List<String> samples;
    // List<Double> highlightedExpression;
    // List<String> clusterNames;
    List<Double> sum;

}
