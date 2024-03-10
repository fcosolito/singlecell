package com.lifescs.singlecell.dto.query;

import java.util.List;

import com.lifescs.singlecell.model.GeneExpression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapClusterLoadDto {
    private String barcode;
    private List<GeneExpression> expressions;
}
