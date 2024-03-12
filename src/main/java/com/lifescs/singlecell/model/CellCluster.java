package com.lifescs.singlecell.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellCluster {
    @DBRef
    private Cluster cluster;
    @DBRef
    private Resolution resolution;
}
