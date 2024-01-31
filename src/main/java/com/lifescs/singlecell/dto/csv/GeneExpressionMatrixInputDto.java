package com.lifescs.singlecell.dto.csv;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneExpressionMatrixInputDto {
    private List<GeneExpressionDto> geneExpressionList;
    private List<String> comments;
    // If this is actually needed for something
    // then it should be parsed in the DAO and asigned to diferent field
    // in this DTO
    private String countsRow;

    public GeneExpressionMatrixInputDto() {
        this.geneExpressionList = new ArrayList<>();
        this.comments = new ArrayList<>();
    }
}
