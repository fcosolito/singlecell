package com.lifescs.singlecell.dto.query;

import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViolinGroupLoadDto {
  private ObjectId cell;
  private ObjectId sample;
  private ObjectId cluster;
}
