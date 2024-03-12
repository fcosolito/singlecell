package com.lifescs.singlecell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Document
@NoArgsConstructor
public class Resolution {
    @Id
    private String id;
    @Field
    private String name;
    @DBRef
    private Experiment experiment;

    // TODO let mongo generate the id
    public Resolution(String name, Experiment experiment) {
        this.name = name;
        this.id = experiment.getId() + name;
    }

}
