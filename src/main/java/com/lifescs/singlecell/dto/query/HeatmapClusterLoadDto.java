package com.lifescs.singlecell.dto.query;

import java.util.List;

import com.lifescs.singlecell.model.CellExpression;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapClusterLoadDto {
    private String barcode;
    private List<CellExpression> expressions;
}
