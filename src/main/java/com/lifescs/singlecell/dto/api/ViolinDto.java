package com.lifescs.singlecell.dto.api;

import java.util.ArrayList;
import java.util.List;

import com.lifescs.singlecell.model.GeneExpression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViolinDto {
  private String cluster;
  private String sample;
  private String code;
  private List<Double> expressions;

  public ViolinDto() {
    this.expressions = new ArrayList<>();
  }
}
