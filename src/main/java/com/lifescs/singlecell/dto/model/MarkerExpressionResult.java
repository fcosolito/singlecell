package com.lifescs.singlecell.dto.model;

import java.util.ArrayList;
import java.util.List;

import com.lifescs.singlecell.model.GeneExpression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkerExpressionResult {
    private List<GeneExpression> expressionList;
    private List<String> clusterMarkers;

    public MarkerExpressionResult() {
        this.clusterMarkers = new ArrayList<>();
        this.expressionList = new ArrayList<>();
    }
}
