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
public class Project {
    @Id
    private String id;
    @Field
    private String name;
    @Field
    private String description;
    @DocumentReference
    private List<Experiment> experiments;

    public Project() {
        this.experiments = new ArrayList<>();
    }

}
