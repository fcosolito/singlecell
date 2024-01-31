package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
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
    @DocumentReference
    private Resolution resolution;
    @Field
    private List<MarkerGene> markers;

    public Cluster(String name, Resolution resolution) {
        this.name = name;
        this.markers = new ArrayList<>();
        this.id = resolution.getId() + name;
    }

    public String toString() {
        return "Cluster :" + id + " res: ";
    }

}
