package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class Cluster {
    @Id
    private String id;
    @Field
    private String name;
    @DBRef
    private Resolution resolution;
    @Field
    private List<MarkerGene> markers;

    public Cluster(String name, Resolution resolution) {
        this.name = name;
        this.resolution = resolution;
        this.markers = new ArrayList<>();
    }

}
