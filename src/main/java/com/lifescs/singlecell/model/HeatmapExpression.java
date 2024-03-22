package com.lifescs.singlecell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapExpression {
    private String bucketId;
    private String code;
    private Double expression;

}
