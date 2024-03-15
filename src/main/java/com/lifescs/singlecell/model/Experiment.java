package com.lifescs.singlecell.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
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
    @DBRef
    private Project project;
    @Transient
    private List<Cell> cells;

    public Experiment(String name) {
        this.name = name;
    }

}
