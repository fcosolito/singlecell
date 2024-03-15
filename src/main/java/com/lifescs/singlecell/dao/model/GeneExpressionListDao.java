package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsDontMatch;
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsMatch;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
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
// Cannot load two experiments at the same time
public class GeneExpressionListDao {
    private GeneExpressionListRepository repository;
    private CellDao cellDao;
    private MongoTemplate mongoTemplate;
    private Map<Integer, PartialGeneExpressionList> inputId2Partial;
    private Map<Integer, Cell> inputId2Cell;

    public List<GeneExpressionList> findListsByExperiment(Experiment e) {
        Query query = Query.query(Criteria.where("experiment.$id").is(e.getId()));
        List<GeneExpressionList> result = mongoTemplate.find(query, GeneExpressionList.class);
        if (result.isEmpty())
            throw new NoObjectFoundException("No cell expression lists found for experiment: " + e.getId());
        else
            return result;
    }

    public void deleteListsByExperiment(Experiment e) {
        repository.deleteAll(findListsByExperiment(e));
    }

    public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) throws Exception {
        this.inputId2Partial = new HashMap<>();

        for (Entry<Integer, String> entry : geneMap.entrySet()) {
            PartialGeneExpressionList partial = new PartialGeneExpressionList();
            partial.setExperiment(e);
            partial.setCode(entry.getValue());
            this.inputId2Partial.put(entry.getKey(), partial);
        }

        // Get cells for this experiment
        List<Cell> cells = cellDao.findCellsByExperiment(e);

        this.inputId2Cell = cells.stream().collect(Collectors.toMap(cell -> cell.getLocalId(), cell -> cell));
    }

    public void endExpressionLoad() {
        this.inputId2Partial = null;
        this.inputId2Cell = null;
    }

    public void bulkSaveExpressions(Experiment esp, List<GeneExpressionDto> expressionList) {
        // bulk update
        Map<Integer, PartialGeneExpressionList> insertMap = new HashMap<>();
        for (GeneExpressionDto ex : expressionList) {
            PartialGeneExpressionList partial = insertMap.get(ex.getGeneId());
            if (partial == null) {
                partial = inputId2Partial.get(ex.getGeneId());
                if (partial == null)
                    throw new RuntimeException("Could not get partial for gene: " + ex.getGeneId());
            }
            insertMap.put(ex.getGeneId(), partial);
            GeneExpression ge = new GeneExpression(inputId2Cell.get(ex.getCellId()), ex.getExpression());
            partial.getExpressions().add(ge);
        }
        List<PartialGeneExpressionList> insertList = insertMap.values().stream().toList();
        log.info("List size: " + insertList.size());

        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
                PartialGeneExpressionList.class);

        ops.insert(insertList);
        ops.execute();
        insertMap = null;
        insertList = null;

    }

    public void fillExpressionLists() {
        log.info("Filling gene expression lists");
        GroupOperation groupPartials = Aggregation.group("experiment", "code")
                .push("expressions").as("expressions");

        ProjectionOperation projectLists = Aggregation.project()
                .and("_id.experiment").as("experiment")
                .and("_id.code").as("code")
                .and(ArrayOperators.Reduce.arrayOf("expressions").withInitialValue(new ArrayList<>())
                        .reduce(ArrayOperators.ConcatArrays.arrayOf("$$value").concat("$$this")))
                .as("expressions");

        MergeOperation merge = Aggregation.merge().intoCollection("geneExpressionList")
                .on("experiment", "code").whenMatched(WhenDocumentsMatch.failOnMatch())
                .whenDocumentsDontMatch(WhenDocumentsDontMatch.insertNewDocument()).build();

        Aggregation aggregation = Aggregation.newAggregation(
                groupPartials,
                projectLists,
                merge).withOptions(AggregationOptions.builder().allowDiskUse(true).skipOutput().build());

        mongoTemplate.aggregate(aggregation, "partialGeneExpressionList",
                Document.class);
    }

}
