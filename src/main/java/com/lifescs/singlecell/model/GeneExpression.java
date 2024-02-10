package com.lifescs.singlecell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Document
public class GeneExpression {
    @Id
    private String id;
    @Indexed
    private String geneCode;
    private Double expression;

    public GeneExpression(String geneCode, Double expression) {
        this.geneCode = geneCode;
        this.expression = expression;
    }
}
