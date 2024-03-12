package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter
@Setter
public class CellExpressionList {
    @Id
    private String id;
    @DBRef
    private Cell cell;
    @Field
    private ExpressionType type;
    @Field
    private List<CellExpression> expressions;

    public CellExpressionList() {
        this.expressions = new ArrayList<>();
    }

    public CellExpressionList(String id) {
        this.id = id;
        this.expressions = new ArrayList<>();
    }
}
