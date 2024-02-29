package com.lifescs.singlecell.dto.query;

import java.util.List;

import com.lifescs.singlecell.model.GeneExpression;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ViolinQueryDto {
    private String clusterId;
    private String sampleId;
    private Integer cellCount;
    private List<List<GeneExpression>> expressionLists;

}
