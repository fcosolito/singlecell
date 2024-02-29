package com.lifescs.singlecell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CellExpression {
    private String cellId;
    private Double expression;
}
