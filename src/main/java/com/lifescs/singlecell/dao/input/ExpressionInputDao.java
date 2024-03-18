package com.lifescs.singlecell.dao.input;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dao.model.CellExpressionListDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dto.input.CellExpressionDto;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellExpression;
import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.ExpressionType;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.GeneExpressionList;
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
    private CellDao cellDao;
    private CellExpressionListDao cellExpressionListDao;
    private GeneExpressionListDao geneExpressionListDao;

    public void loadCellExpressions(Project p, Experiment e) throws IOException {
        Map<String, Cell> barcode2Cell = cellDao.findCellsByExperiment(e).stream()
        .collect(Collectors.toMap(cell -> cell.getBarcode(), cell -> cell));

        try (Reader reader = Files.newBufferedReader(Path.of(pathMapper.cellMatrixPath(p, e)))) {
            CsvToBean<CellExpressionDto> cb = new CsvToBeanBuilder<CellExpressionDto>(reader)
                    .withType(CellExpressionDto.class)
                    .build();
            Iterator<CellExpressionDto> i = cb.iterator();
            CellExpressionDto dto;
            int count = 0;
            long start = System.nanoTime();
            while (i.hasNext()) {
                CellExpressionList expressionList = new CellExpressionList();

                dto = i.next();
                expressionList.setExpressions(dto.getExpressions().entries().stream().map(entry -> new CellExpression(entry.getKey(), entry.getValue()))
                .toList());
                expressionList.setCell(barcode2Cell.get(dto.getBarcode()));
                expressionList.setType(new ExpressionType("gene"));
                cellExpressionListDao.save(expressionList);

                count++;
                dto = null;

            }
            long end = System.nanoTime();
            log.info("Time getting cell expressions: " + (end - start) / 1_000_000_000.0 + " seconds");
            log.info("Count: " + count);
        }

    }

    public void loadGeneExpressions(Project p, Experiment e) throws IOException {
        Map<String, Cell> barcode2Cell = cellDao.findCellsByExperiment(e).stream()
        .collect(Collectors.toMap(cell -> cell.getBarcode(), cell -> cell));

        try (Reader reader = Files.newBufferedReader(Path.of(pathMapper.geneMatrixPath(p, e)))) {
            CsvToBean<GeneExpressionDto> cb = new CsvToBeanBuilder<GeneExpressionDto>(reader)
                    .withType(GeneExpressionDto.class)
                    .build();
            Iterator<GeneExpressionDto> i = cb.iterator();
            GeneExpressionDto dto;
            int count = 0;
            long start = System.nanoTime();
            while (i.hasNext()) {
                GeneExpressionList expressionList = new GeneExpressionList();

                dto = i.next();
                expressionList.setExpressions(dto.getExpressions().entries().stream().map(entry -> 
                new GeneExpression(barcode2Cell.get(entry.getKey()), entry.getValue())).toList());
                expressionList.setCode(dto.getGenecode());
                expressionList.setExperiment(e);
                geneExpressionListDao.save(expressionList);

                count++;
                dto = null;
            }
            long end = System.nanoTime();
            log.info("Time getting gene expressions: " + (end - start) / 1_000_000_000.0 + " seconds");
            log.info("Count: " + count);
        }

    }
}
