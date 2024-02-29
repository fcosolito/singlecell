package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter
@Setter
public class CellExpressionList {
    @Id
    private ObjectId id;
    @Field
    @Indexed
    private String experimentId;
    @Field
    @Indexed
    private String geneCode;
    @Field
    private List<CellExpression> expressions;

    public CellExpressionList() {
        this.expressions = new ArrayList<>();
    }
}
