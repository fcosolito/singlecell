package com.lifescs.singlecell.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneExpressionDto {
    Integer geneId;
    Integer cellId;
    Double expression;

}
