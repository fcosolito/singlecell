package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class MarkerExpressionList {
    @Id
    public ObjectId id;
    @Field
    public List<GeneExpression> markerExpressions;
    @DocumentReference
    public Resolution resolution;

    public MarkerExpressionList() {
        this.markerExpressions = new ArrayList<>();
    }

}