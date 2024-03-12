package com.lifescs.singlecell.model;

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
public class HeatmapCluster {
    @Id
    private String id;
    @Field
    private List<String> buckets;
    @Field
    private List<HeatmapExpression> expressions;
    @Field
    private List<String> topMarkers;
    @DBRef
    private Cluster cluster;

}
