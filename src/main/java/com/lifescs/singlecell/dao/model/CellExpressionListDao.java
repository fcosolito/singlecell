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
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
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
@Slf4j
public class CellExpressionListDao {
    private CellExpressionListRepository repository;
    private MongoTemplate mongoTemplate;
    private Map<Integer, ObjectId> loadMap;
    private Map<Integer, String> cellMap;

    public CellExpressionListDao(CellExpressionListRepository repository,
            MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
    }

    public void saveExpressionLists(List<CellExpressionList> expressions) {
        repository.saveAll(expressions);
    }

    public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) throws Exception {
        Map<Integer, ObjectId> loadMap = new HashMap<>();
        List<CellExpressionList> lists = new ArrayList<>();
        for (Entry<Integer, String> entry : geneMap.entrySet()) {
            ObjectId id = new ObjectId();
            CellExpressionList cel = new CellExpressionList(id);
            cel.setExperimentId(e.getId());
            cel.setGeneCode(entry.getValue());
            lists.add(cel);
            e.getExpressionsByGeneIds().add(id);
            loadMap.put(entry.getKey(), id);
        }
        saveExpressionLists(lists);
        this.loadMap = loadMap;
        this.cellMap = e.getCells().stream()
                .collect(Collectors.toMap(cell -> cell.getLocalId(), cell -> cell.getId()));
    }

    public void endExpressionLoad() {
        this.loadMap = null;
        this.cellMap = null;
    }

    public void bulkSaveExpressions(Experiment e, List<GeneExpressionDto> expressionList) {
        long start = System.nanoTime();
        // bulk update
        Map<ObjectId, List<CellExpression>> map = new HashMap<>();
        expressionList.forEach(ex -> {
            List<CellExpression> list = map.get(loadMap.get(ex.getGeneId()));
            if (list == null) {
                list = new ArrayList<>();
                map.put(loadMap.get(ex.getGeneId()), list);
            }
            CellExpression ce = new CellExpression(cellMap.get(ex.getCellId()), ex.getExpression());
            list.add(ce);
        });
        // Create a 'updateOne' operation for each list
        // BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
        // CellExpressionList.class);
        // for (Entry<ObjectId, List<CellExpression>> entry : map.entrySet()) {
        // ops.updateOne(Query.query(Criteria.where("_id").is(entry.getKey())),
        // new Update().push("expressions").each(entry.getValue()));
        // }
        log.info("Inserting into " + map.values().size() + " cell expression lists");
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
                PartialCellExpressionList.class);
        List<PartialCellExpressionList> insertList = new ArrayList<>();
        for (Entry<ObjectId, List<CellExpression>> entry : map.entrySet()) {
            PartialCellExpressionList partial = new PartialCellExpressionList(entry.getKey(), entry.getValue());
            insertList.add(partial);
            // ops.insert(partial);
        }
        ops.insert(insertList);
        ops.execute();
        long end = System.nanoTime();
        log.info("Time spent processing cell expressions: " + (end - start) / 1_000_000_000.0 + " seconds");
        log.info("Ended: " + System.currentTimeMillis());

    }

    public void fillExpressionList(ObjectId id) {
        MatchOperation matchPartial = Aggregation.match(Criteria.where("cellExpressionListId").is(id));
        GroupOperation groupByList = Aggregation.group("cellExpressionListId")
                .push("expressions").as("expressions");

        ProjectionOperation reduceExpressions = Aggregation.project()
                .and(ArrayOperators.Reduce.arrayOf("expressions").withInitialValue(new ArrayList<>())
                        .reduce(ArrayOperators.ConcatArrays.arrayOf("$$value").concat("$$this")))
                .as("expressions");

        Aggregation aggregation = Aggregation.newAggregation(
                matchPartial,
                groupByList,
                reduceExpressions);

        ExpressionsResult result = mongoTemplate
                .aggregate(aggregation, "partialCellExpressionList", ExpressionsResult.class).getUniqueMappedResult();

        if (result != null) {
            log.info(result.expressions.get(0).getCellId());
            UpdateResult ur = mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)),
                    new Update().set("expressions", result.expressions), "cellExpressionList");
            log.info("Matched count: " + ur.getMatchedCount());
        } else {
            throw new NoObjectFoundException("No partial expressions found for " + id);
        }
    }

    class ExpressionsResult {
        private List<CellExpression> expressions;
    }
}
