package com.lifescs.singlecell.dto.model;

import java.util.List;

import com.lifescs.singlecell.model.HeatmapExpression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapDto {
    private String name;
    private List<String> markers;
    private List<String> buckets;
    private List<HeatmapExpression> expressions;
}
