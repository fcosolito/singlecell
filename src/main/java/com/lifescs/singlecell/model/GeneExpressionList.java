package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
@CompoundIndex(name = "expression_genecode", def = "{'geneExpressions.geneCode':1}")
public class GeneExpressionList {
    @Id
    private String id;
    @Field
    private List<GeneExpression> geneExpressions;

    public GeneExpressionList() {
        this.geneExpressions = new ArrayList<>();
    }

}
