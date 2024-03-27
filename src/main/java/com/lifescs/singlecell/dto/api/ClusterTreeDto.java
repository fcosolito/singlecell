package com.lifescs.singlecell.dto.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterTreeDto {
  private String resolution;
  private String cluster;
  private Integer cellCount;

}
