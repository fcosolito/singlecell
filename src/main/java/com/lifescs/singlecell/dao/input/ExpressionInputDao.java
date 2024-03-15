package com.lifescs.singlecell.dao.input;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.input.CellExpressionDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ExpressionInputDao extends CsvDao<CellExpressionDto> {
    private PathMapper pathMapper;

    public CsvToBean<CellExpressionDto> getCsvToBean(Project p, Experiment e) throws IOException {
        return getCsvToBean(Path.of(pathMapper.matrixPath(p, e)),
                CellExpressionDto.class);
    }

    public CellExpressionDto readOne(Project p, Experiment e) throws IOException {
        return readOne(Path.of(pathMapper.matrixPath(p, e)),
                CellExpressionDto.class);
    }

    public List<CellExpressionDto> readCSVToMetadataBeans(Project p, Experiment e) throws IOException {
        return readCSVToBeans(Path.of(pathMapper.matrixPath(p, e)),
                CellExpressionDto.class);
    }

    public void loadCellExpressions(Project p, Experiment e) throws IOException {

        try (Reader reader = Files.newBufferedReader(Path.of(pathMapper.matrixPath(p, e)))) {
            CsvToBean<CellExpressionDto> cb = new CsvToBeanBuilder<CellExpressionDto>(reader)
                    .withType(CellExpressionDto.class)
                    .build();
            Iterator<CellExpressionDto> i = cb.iterator();
            CellExpressionDto dto = null;
            int count = 0;
            long start = System.nanoTime();
            while (i.hasNext()) {
                dto = i.next();
                count++;
                // Save to db

            }
            long end = System.nanoTime();
            log.info("Time getting expressions: " + (end - start) / 1_000_000_000.0 + " seconds");
            log.info("Count: " + count);
        }

    }
}
