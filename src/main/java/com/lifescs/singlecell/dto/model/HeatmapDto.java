package com.lifescs.singlecell.dto.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeatmapDto {
    List<HeatmapClusterDto> clusters;

}
