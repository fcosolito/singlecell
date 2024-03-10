package com.lifescs.singlecell.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class PartialCellExpressionList {
    @Id
    private ObjectId id;
    @Field
    // @Indexed
    private ObjectId cellExpressionListId;
    @Field
    private List<CellExpression> expressions;

    public PartialCellExpressionList(ObjectId expressionListId, List<CellExpression> expressions) {
        this.cellExpressionListId = expressionListId;
        this.expressions = expressions;
    }

}
