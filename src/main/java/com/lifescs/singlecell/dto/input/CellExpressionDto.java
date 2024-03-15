package com.lifescs.singlecell.dto.input;

import org.apache.commons.collections4.MultiValuedMap;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CellExpressionDto extends CsvBean {
    @CsvBindByName(column = "barcode")
    private String barcode;
    // Map of (code, expression)
    @CsvBindAndJoinByName(column = ".*", elementType = Double.class)
    private MultiValuedMap<String, Double> expressions;
}
