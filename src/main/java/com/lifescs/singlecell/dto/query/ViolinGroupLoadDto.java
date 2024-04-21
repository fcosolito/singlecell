package com.lifescs.singlecell.dto.query;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViolinGroupLoadDto {
  private ObjectId sample;
  private ObjectId resolution;
  private ObjectId cluster;
  private List<ObjectId> cells;
}
