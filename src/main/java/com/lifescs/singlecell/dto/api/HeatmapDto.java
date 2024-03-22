package com.lifescs.singlecell.dto.api;

import java.util.List;

import com.lifescs.singlecell.model.HeatmapExpression;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeatmapDto {
    private String name;
    private List<String> markers;
    private List<String> buckets;
    private List<HeatmapExpression> expressions;
}
