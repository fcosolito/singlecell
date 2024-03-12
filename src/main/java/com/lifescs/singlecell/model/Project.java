package com.lifescs.singlecell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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

    public Project(String id) {
        this.id = id;
    }

}
