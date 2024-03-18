package com.lifescs.singlecell.dao.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dao.model.CellExpressionListDao;
import com.lifescs.singlecell.dao.model.GeneExpressionListDao;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.dto.input.GeneMapDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
// This is no longer used
public class GeneExpressionMatrixInputDao extends CsvDao<GeneMapDto> {
    private GeneExpressionListDao geneExpressionListDao;
    private CellExpressionListDao cellExpressionListDao;
    private PathMapper pathMapper;

  /*
    public void readMatrix(Project p, Experiment e, Long chunckSize) throws Exception {
        // Create an expression list for each cell in the experiment
        cellExpressionListDao.startExpressionLoad(e, readGeneMapping(p, e));

        // Create an expression list for each gene in the gene map
        geneExpressionListDao.startExpressionLoad(e, readGeneMapping(p, e));

        log.info("Reading Matrix file with chunk size: " + chunckSize / 1_000_000.0 + "M lines");
        try (BufferedReader bufferedReader = new BufferedReader(
                new FileReader(pathMapper.geneExpressionMatrixPath(p, e)))) {
            String line = null;
            Boolean commentSection = true;
            List<GeneExpressionDto> expressionList = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null && commentSection) {
                if (line.charAt(0) == '%') {
                    // do nothing
                } else {
                    commentSection = false;
                }
            }
            int chunkCount = 0;
            long lineCount = 0;
            long start = System.nanoTime();
            // int poolSize = 4;
            // ExecutorService service = Executors.newFixedThreadPool(poolSize);
            while ((line = bufferedReader.readLine()) != null) {
                GeneExpressionDto ge = new GeneExpressionDto();
                String[] elements = line.split("\\s+");
                ge.setCellId(Integer.parseInt(elements[0]));
                ge.setGeneId(Integer.parseInt(elements[1]));
                ge.setExpression(Double.parseDouble(elements[2]));
                expressionList.add(ge);
                lineCount++;
                if (lineCount >= chunckSize) {
                    log.info("Processing chunk: " + chunkCount);

                    // Create threads to load gene and cell expressions in parallel
                    Thread geneThread = new Thread(new SaveGeneExpressions(geneExpressionListDao,
                            e, expressionList));
                    geneThread.start();
                    Thread cellThread = new Thread(new SaveCellExpressions(cellExpressionListDao,
                            e, expressionList));
                    cellThread.start();
                    geneThread.join();
                    cellThread.join();

                    // empty expression list
                    expressionList = null;
                    expressionList = new ArrayList<>();
                    lineCount = 0;
                    chunkCount++;

                }

            }
            log.info("Processing last chunk: " + chunkCount + " with " + lineCount + " lines");
            geneExpressionListDao.bulkSaveExpressions(e, expressionList);
            cellExpressionListDao.bulkSaveExpressions(e, expressionList);
            geneExpressionListDao.endExpressionLoad();
            cellExpressionListDao.endExpressionLoad();

            long end = System.nanoTime();
            Double totalExpressions = (chunkCount * chunckSize + lineCount) / 1_000_000.0;
            Double elapsedTime = (end - start) / 1_000_000_000.0;
            log.info("Finished saving " + totalExpressions + "M expressions in " + elapsedTime + " seconds");
            log.info("Average saving rate: " + totalExpressions / elapsedTime + "M/s");

        }

    }

    // Reads csv file with gene mappings (matrix column) -> (geneCode)
    public Map<Integer, String> readGeneMapping(Project p, Experiment e) throws Exception {
        List<GeneMapDto> geneList = readCSVToBeans(Path.of(pathMapper.geneMapPath(p, e)), GeneMapDto.class);
        return geneList.stream()
                .collect(Collectors.toMap(g -> g.getId(), g -> g.getCode()));
    }
  */

}
