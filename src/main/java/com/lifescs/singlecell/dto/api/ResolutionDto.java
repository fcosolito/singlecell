package com.lifescs.singlecell.dto.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolutionDto {
    private String resolutionName;
    private String clusterName;

    public String toString() {
        return "Res: " + resolutionName + ", clus: " + clusterName;
    }
}
