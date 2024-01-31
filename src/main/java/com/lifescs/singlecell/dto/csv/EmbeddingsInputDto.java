package com.lifescs.singlecell.dto.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddingsInputDto extends CsvBean {

    @CsvBindByName(column = "cell_id")
    private String cellId;
    @CsvBindByName(column = "spring1")
    private Double spring1;
    @CsvBindByName(column = "spring2")
    private Double spring2;
    @CsvBindByName(column = "tsne1")
    private Double tsne1;
    @CsvBindByName(column = "tsne2")
    private Double tsne2;
    @CsvBindByName(column = "pca1")
    private Double pca1;
    @CsvBindByName(column = "pca2")
    private Double pca2;
    @CsvBindByName(column = "umap1")
    private Double umap1;
    @CsvBindByName(column = "umap2")
    private Double umap2;

}
