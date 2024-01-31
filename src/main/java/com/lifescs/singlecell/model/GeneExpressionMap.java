package com.lifescs.singlecell.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneExpressionMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Double> map;
    private String experimentId;

}
