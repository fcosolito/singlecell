package com.lifescs.singlecell.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneExpression {
    @DBRef
    private Cell cell;
    private Double expression;

}
