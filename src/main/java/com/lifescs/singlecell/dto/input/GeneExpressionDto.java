package com.lifescs.singlecell.dto.input;


import org.apache.commons.collections4.MultiValuedMap;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneExpressionDto extends CsvBean{
    @CsvBindByName(column = "genecode")
    private String code;
    @CsvBindAndJoinByName(column = ".*", elementType = Double.class)
    private MultiValuedMap<String, Double> expressions;
}
/* 
public class GeneExpressionDto {
    Integer geneId;
    Integer cellId;
    Double expression;

}
*/
