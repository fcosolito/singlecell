package com.lifescs.singlecell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneExpression {
    private String geneCode;
    private Double expression;

    public String toString() {
        return "GE: " + geneCode + ", " + expression.toString();
    }
}
