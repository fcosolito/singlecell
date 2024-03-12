package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter
@Setter
public class PartialGeneExpressionList {

    @Id
    private String id;
    @Indexed
    private String code;
    @DBRef
    private Experiment experiment;
    @Field
    private List<GeneExpression> expressions;

    public PartialGeneExpressionList() {
        this.expressions = new ArrayList<>();
    }
}
