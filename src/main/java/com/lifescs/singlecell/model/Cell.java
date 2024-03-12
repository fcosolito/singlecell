package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("cell")
@CompoundIndexes({
        @CompoundIndex(name = "cell_to_experiment", def = "{'experiment.id' : 1}")
})
public class Cell {
    @Id
    private String id;
    @DBRef
    private Experiment experiment;
    @Field
    private Integer localId;
    @Field
    private String barcode;
    @DBRef
    private Sample sample;
    @Field
    private Double percentOfMitochondrialGenes;
    @Field
    private Integer numberOfUMIs;
    @Field
    private Integer numberOfgenes;
    @Field
    private String cellNameLow;
    @Field
    private String cellNameHigh;
    @Field
    private Double spring1;
    @Field
    private Double spring2;
    @Field
    private Double tsne1;
    @Field
    private Double tsne2;
    @Field
    private Double pca1;
    @Field
    private Double pca2;
    @Field
    private Double umap1;
    @Field
    private Double umap2;
    @Field
    private List<CellCluster> cellClusters;

    public Cell() {
        this.cellClusters = new ArrayList<>();
    }

}
