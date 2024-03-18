package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
// Indexes must be created manually either with mongosh or mongo template
// A compound unique index {experiment:1, code:1} is required to load
// expressions
public class GeneExpressionList {
    @Id
    private String id;
    @Field
    private String code;
    @DBRef
    private Experiment experiment;
    @Field
    private List<GeneExpression> expressions;

    public GeneExpressionList() {
        this.expressions = new ArrayList<>();
    }

    public GeneExpressionList(String id) {
        this.expressions = new ArrayList<>();
        this.id = id;
    }

}
