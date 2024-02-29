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
public class Experiment {
    @Id
    private String id;
    @Field
    private String name;
    @Field
    private String description;
    @Field
    private List<ObjectId> expressionsByGeneIds;
    @DocumentReference(lazy = true)
    private List<Cell> cells;
    @DocumentReference
    private List<Sample> samples;
    @DocumentReference
    private List<Resolution> resolutions;

    public Experiment(String name) {
        this.name = name;
        this.expressionsByGeneIds = new ArrayList<>();
        this.cells = new ArrayList<>();
        this.samples = new ArrayList<>();
        this.resolutions = new ArrayList<>();
    }

}
