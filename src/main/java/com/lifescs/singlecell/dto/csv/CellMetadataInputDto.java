package com.lifescs.singlecell.dto.csv;

import org.apache.commons.collections4.MultiValuedMap;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellMetadataInputDto extends CsvBean {
    // An opencsv bean
    @CsvBindByName(column = "id")
    private Integer id;
    @CsvBindByName(column = "cell_id")
    private String barcode;
    @CsvBindByName(column = "sampleNames")
    private String sample;
    @CsvBindByName(column = "percentMT")
    private Double percentOfMitochondrialGenes;
    @CsvBindByName(column = "numberUMIs")
    private Integer numberOfUMIs;
    @CsvBindByName(column = "numberGenes")
    private Integer numberOfGenes;
    @CsvBindByName(column = "cellNamesLow")
    private String cellNameLow;
    @CsvBindByName(column = "cellNamesHigh")
    private String cellNameHigh;
    // EXP2
    @CsvBindAndJoinByName(column = "cluster_[0-9].[0-9]+", elementType = String.class)
    // @CsvBindAndJoinByName(column = "[0-9].[0-9]+", elementType = String.class)
    private MultiValuedMap<String, String> clusters;
    @CsvBindByName(column = "spring1")
    private Double spring1;
    @CsvBindByName(column = "spring2")
    private Double spring2;
    @CsvBindByName(column = "pca1")
    private Double pca1;
    @CsvBindByName(column = "pca2")
    private Double pca2;
    @CsvBindByName(column = "umap1")
    private Double umap1;
    @CsvBindByName(column = "umap2")
    private Double umap2;
    @CsvBindByName(column = "tsne1")
    private Double tsne1;
    @CsvBindByName(column = "tsne2")
    private Double tsne2;

    @Override
    public String toString() {
        return "CellMetadataInputDto [barcode=" + barcode + ", sample=" + sample + ", numberOfGenes=" + numberOfGenes
                + "], clusters: " + clusters.toString();
    }

}
