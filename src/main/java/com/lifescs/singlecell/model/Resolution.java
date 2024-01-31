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
public class Resolution {
    @Id
    private String id;
    @Field
    private String name;
    @DocumentReference
    private List<Cluster> clusters;

    public Resolution() {

    }

    public Resolution(String name, Experiment experiment) {
        this.name = name;
        this.clusters = new ArrayList<>();
        this.id = experiment.getId() + name;
    }

    public String toString() {
        String s = "Resolution: " + id + " w clusters: ";
        for (Cluster c : clusters) {
            s = s + c.toString() + " ";
        }
        return s;
    }
}
