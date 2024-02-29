package com.lifescs.singlecell.dto.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopMarkerDto {
    private String clusterId;
    private List<String> topMarkers;

    public String toString() {
        return "Top " + clusterId;
    }

}
