package com.lifescs.singlecell.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class PartialGeneExpressionList {
    @Id
    private ObjectId id;
    @Field
    // @Indexed
    private ObjectId geneExpressionListId;
    @Field
    private List<GeneExpression> expressions;

    public PartialGeneExpressionList(ObjectId expressionListId, List<GeneExpression> expressions) {
        this.geneExpressionListId = expressionListId;
        this.expressions = expressions;
    }

}
