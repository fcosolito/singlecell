package com.lifescs.singlecell.model;

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
public class User {
    @Id
    private String id;
    @Field
    private String username;
    @Field
    private String password;
    @Field
    private String emailAddress;
    @DocumentReference
    private List<Project> projects;

}
