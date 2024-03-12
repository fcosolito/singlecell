package com.lifescs.singlecell.dto.input;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneMapDto extends CsvBean {
    @CsvBindByName(column = "id")
    public Integer id;
    @CsvBindByName(column = "code")
    public String code;

}
