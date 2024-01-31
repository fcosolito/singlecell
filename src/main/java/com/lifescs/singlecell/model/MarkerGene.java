package com.lifescs.singlecell.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkerGene {
    private String geneCode;
    private Double percent1;
    private Double percent2;
    private Double foldChange;
    private Double pValue;
    private Double adjacentPValue;

}
