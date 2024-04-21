package com.lifescs.singlecell.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class ViolinGroup {
  @Id
  private ObjectId id;
  @Field
  private ObjectId resolutionId;
  @Field
  private ObjectId clusterId;
  @Field
  private ObjectId sampleId;
  @Field
  private String code;
  @Field
  private List<Double> expressions;

  public ViolinGroup(){
    this.expressions = new ArrayList<>();
  }
}
