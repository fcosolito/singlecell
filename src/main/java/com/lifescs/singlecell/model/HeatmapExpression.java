package com.lifescs.singlecell.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeatmapExpression {
    private String bucketId;
    private String code;
    private Double expression;

    public HeatmapExpression(String bucketId, String geneCode) {
        this.bucketId = bucketId;
        this.code = geneCode;
        this.expression = 0.0;
    }
}
