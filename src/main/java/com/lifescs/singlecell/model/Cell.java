package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("cell")
public class Cell {

    @Id
    private String id;
    @Field
    private Integer localId;
    @Field
    private String barcode;
    @DocumentReference
    private Sample sample;
    @DocumentReference(lazy = true)
    private GeneExpressionList geneExpressions;
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
    @DocumentReference
    private List<Cluster> clusters;

    public Cell() {
        this.clusters = new ArrayList<>();
        this.geneExpressions = new GeneExpressionList();
    }

    @Override
    public String toString() {
        return "Cell: " + localId + ": (" + barcode + ") from sample: " + sample.getName();
    }

}
