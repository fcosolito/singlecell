package com.lifescs.singlecell.dao.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.dto.csv.GeneExpressionMatrixInputDto;
import com.lifescs.singlecell.dto.csv.GeneMapDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

@Component
public class GeneExpressionMatrixInputDao extends CsvDao<GeneMapDto> {

    private Logger logger;
    private PathMapper pathMapper;

    public GeneExpressionMatrixInputDao(PathMapper pathMapper) {
        this.pathMapper = pathMapper;
        this.logger = LoggerFactory.getLogger(GeneExpressionMatrixInputDao.class);
    }

    public GeneExpressionMatrixInputDto readMatrix(Project p, Experiment e) throws Exception {
        GeneExpressionMatrixInputDto matrix = new GeneExpressionMatrixInputDto();

        logger.info("Reading Matrix");
        try (BufferedReader bufferedReader = new BufferedReader(
                new FileReader(pathMapper.geneExpressionMatrixPath(p, e)))) {
            String line = null;
            Boolean commentSection = true;
            List<String> comments = new ArrayList<>();
            List<GeneExpressionDto> expressionList = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null && commentSection) {
                if (line.charAt(0) == '%') {
                    comments.add(line);
                } else {
                    commentSection = false;
                    matrix.setComments(comments);

                    // The first row after comments has the counts
                    matrix.setCountsRow(line);

                }
            }
            while ((line = bufferedReader.readLine()) != null) {
                GeneExpressionDto ge = new GeneExpressionDto();
                String[] elements = line.split("\\s+");
                ge.setCellId(Integer.parseInt(elements[0]));
                ge.setGeneId(Integer.parseInt(elements[1]));
                ge.setExpression(Double.parseDouble(elements[2]));
                expressionList.add(ge);

            }
            matrix.setGeneExpressionList(expressionList);

            logger.info("Finished reading matrix");
            return matrix;

        }

    }

    public Map<Integer, String> readGeneMapping(Project p, Experiment e) throws Exception {
        List<GeneMapDto> geneList = readCSVToBeans(Path.of(pathMapper.geneMapPath(p, e)), GeneMapDto.class);
        return geneList.stream()
                .collect(Collectors.toMap(g -> g.getId(), g -> g.getCode()));
    }

}
