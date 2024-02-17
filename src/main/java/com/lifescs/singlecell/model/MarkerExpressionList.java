package com.lifescs.singlecell.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class MarkerExpressionList {
    @Id
    public String id;
    @Field
    public List<GeneExpression> markerExpressions;
    @Field
    public Resolution resolution;

}