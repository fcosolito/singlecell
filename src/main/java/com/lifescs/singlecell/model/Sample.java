package com.lifescs.singlecell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Document
public class Sample {

    @EqualsAndHashCode.Exclude
    @Id
    private String id;
    @EqualsAndHashCode.Include
    @Field
    private String name;
    @DBRef
    private Experiment experiment;

    public Sample(String name) {
        this.name = name;
    }

    public String toString() {
        return "Sample: " + id;
    }
}
