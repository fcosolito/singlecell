package com.lifescs.singlecell.dao.input;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public abstract class CsvDao<T> {

    protected List<T> readCSVToBeans(Path path, Class clazz) throws Exception {
        List<T> parseResult;

        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean<T> cb = new CsvToBeanBuilder<T>(reader)
                    .withType(clazz)
                    .build();
            parseResult = cb.parse();
        }
        return parseResult;
    }

}