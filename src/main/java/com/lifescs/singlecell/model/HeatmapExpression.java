package com.lifescs.singlecell.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeatmapExpression {
    private String bucketId;
    private String geneCode;
    private Double expression;

    public HeatmapExpression(String bucketId, String geneCode) {
        this.bucketId = bucketId;
        this.geneCode = geneCode;
        this.expression = 0.0;
    }
}
