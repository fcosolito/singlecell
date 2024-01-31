package com.lifescs.singlecell.dto.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkerGeneInputDto extends CsvBean {
    @CsvBindByName(column = "Gene")
    private String geneCode;
    @CsvBindByName(column = "Percent1")
    private Double percent1;
    @CsvBindByName(column = "Percent2")
    private Double percent2;
    @CsvBindByName(column = "foldChange")
    private Double foldChange;
    @CsvBindByName(column = "pvalue")
    private Double pValue;
    @CsvBindByName(column = "adj.pvalue")
    private Double adjacentPValue;
    @CsvBindByName(column = "Resolution")
    private String resolution;
    @CsvBindByName(column = "Cluster")
    private String cluster;

}
