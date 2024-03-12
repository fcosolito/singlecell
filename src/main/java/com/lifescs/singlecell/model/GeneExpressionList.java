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
@CompoundIndexes(@CompoundIndex(name = "experiment_code", def = "{'experiment.id' : 1, 'code' : 1}"))
public class GeneExpressionList {
    @Id
    private String id;
    @Field
    private String code;
    @DBRef
    private Experiment experiment;
    @Field
    private List<GeneExpression> geneExpressions;

    public GeneExpressionList() {
        this.geneExpressions = new ArrayList<>();
    }

    public GeneExpressionList(String id) {
        this.geneExpressions = new ArrayList<>();
        this.id = id;
    }

}
