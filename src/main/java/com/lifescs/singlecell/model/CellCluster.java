package com.lifescs.singlecell.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellCluster {
    @DBRef(lazy = true)
    private Cluster cluster;
    @DBRef(lazy = true)
    private Resolution resolution;
}
