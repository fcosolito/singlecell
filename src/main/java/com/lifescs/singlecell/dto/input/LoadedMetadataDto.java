package com.lifescs.singlecell.dto.input;

import java.util.ArrayList;
import java.util.List;

import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.Sample;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadedMetadataDto {
    private List<Cell> cells;
    private List<Resolution> resolutions;
    private List<Sample> samples;
    private List<Cluster> clusters;

    public LoadedMetadataDto() {
        this.cells = new ArrayList<>();
        this.resolutions = new ArrayList<>();
        this.samples = new ArrayList<>();
        this.clusters = new ArrayList<>();
    }

}
