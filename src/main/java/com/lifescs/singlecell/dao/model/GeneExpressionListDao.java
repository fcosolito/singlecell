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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellExpression;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.PartialGeneExpressionList;
import com.lifescs.singlecell.repository.GeneExpressionListRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class GeneExpressionListDao {
    private GeneExpressionListRepository repository;
    private MongoTemplate mongoTemplate;
    private Map<Integer, PartialGeneExpressionList> inputId2Partial;
    private Map<Integer, Cell> inputId2Cell;

    public void saveExpressionLists(List<GeneExpressionList> expressions) {
        repository.saveAll(expressions);
    }

    public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) throws Exception {
        this.inputId2Partial = new HashMap<>();

        for (Entry<Integer, String> entry : geneMap.entrySet()) {
            PartialGeneExpressionList partial = new PartialGeneExpressionList();
            partial.setExperiment(e);
            partial.setCode(entry.getValue());
            this.inputId2Partial.put(entry.getKey(), partial);
        }
        // TODO Find cells for the given experiment
        this.inputId2Cell = e.getCells().stream()
                .collect(Collectors.toMap(cell -> cell.getLocalId(), cell -> cell));
    }

    public void endExpressionLoad() {
        this.inputId2Partial = null;
        this.inputId2Cell = null;
    }

    public void bulkSaveExpressions(Experiment e, List<GeneExpressionDto> expressionList) {
        // bulk update
        Map<Integer, PartialGeneExpressionList> insertMap = new HashMap<>();
        expressionList.forEach(ex -> {
            PartialGeneExpressionList partial = insertMap.get(ex.getGeneId());
            if (partial == null) {
                partial = inputId2Partial.get(ex.getGeneId());
                if (partial == null)
                    throw new RuntimeException("Could not get partial for gene: " + ex.getGeneId());
            }
            insertMap.put(ex.getGeneId(), partial);
            GeneExpression ge = new GeneExpression(inputId2Cell.get(ex.getCellId()), ex.getExpression());
            partial.getExpressions().add(ge);
        });

        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
                PartialGeneExpressionList.class);

        ops.insert(insertMap.values());
        ops.execute();

    }

}
