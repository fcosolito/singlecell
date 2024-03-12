package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dao.input.GeneExpressionMatrixInputDao;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellExpression;
import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.PartialCellExpressionList;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.repository.CellExpressionListRepository;
import com.mongodb.client.result.UpdateResult;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CellExpressionListDao {

    private CellExpressionListRepository repository;
    private MongoTemplate mongoTemplate;
    private Map<Integer, PartialCellExpressionList> inputId2Partial;
    private Map<Integer, String> inputId2Code;

    public void saveExpressionLists(List<CellExpressionList> list) {
        repository.saveAll(list);
    }

    public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) {
        this.inputId2Partial = new HashMap<>();
        // TODO get cells
        List<Cell> cells = e.getCells();
        cells.forEach(c -> {
            PartialCellExpressionList partial = new PartialCellExpressionList();
            partial.setCell(c);
            this.inputId2Partial.put(c.getLocalId(), partial);
        });
        this.inputId2Code = geneMap;
    }

    public void endExpressionLoad() {
        this.inputId2Code = null;
        this.inputId2Partial = null;
    }

    public void bulkSaveExpressions(Experiment e, List<GeneExpressionDto> expressionList) {
        // bulk update
        Map<Integer, PartialCellExpressionList> insertMap = new HashMap<>();
        expressionList.forEach(ex -> {
            PartialCellExpressionList partial = insertMap.get(ex.getCellId());
            if (partial == null) {
                partial = inputId2Partial.get(ex.getCellId());
                if (partial == null)
                    throw new RuntimeException("Could not get partial for cell: " + ex.getCellId());
            }

            CellExpression ce = new CellExpression(inputId2Code.get(ex.getGeneId()), ex.getExpression());
            partial.getExpressions().add(ce);
        });

        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
                PartialCellExpressionList.class);

        ops.insert(insertMap.values());
        ops.execute();
    }
}
