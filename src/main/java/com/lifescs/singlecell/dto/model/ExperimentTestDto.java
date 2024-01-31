package com.lifescs.singlecell.dto.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperimentTestDto {

    private String experimentId;
    private List<String> barcodes;
    private List<Integer> umis;

    public String toString() {
        String s = "Experiment: " + experimentId;
        for (int i = 0; i < barcodes.size() - 1; i++) {
            s += " Cell: " + barcodes.get(i) + " umi count: " + umis.get(i) + "\n";

        }
        return s;
    }
}
