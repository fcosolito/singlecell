package com.lifescs.singlecell.dto.api;

import java.util.ArrayList;
import java.util.List;

import com.lifescs.singlecell.model.GeneExpression;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ViolinDto {
    private String clusterId;
    private String sampleId;
    private List<GeneExpression> expressions;

    public ViolinDto() {
        this.expressions = new ArrayList<>();
    }
}
