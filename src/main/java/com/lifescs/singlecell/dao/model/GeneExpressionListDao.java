package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.model.PartialGeneExpressionList;
import com.lifescs.singlecell.repository.GeneExpressionListRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeneExpressionListDao {
    private GeneExpressionListRepository repository;
    private MongoTemplate mongoTemplate;
    private Map<Integer, ObjectId> loadMap;
    private Map<Integer, String> geneMap;

    public GeneExpressionListDao(GeneExpressionListRepository repository, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
    }

    public void saveExpressionLists(List<GeneExpressionList> list) {
        repository.saveAll(list);
    }

    public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) {
        Map<Integer, ObjectId> loadMap = new HashMap<>();
        List<Cell> cells = e.getCells();
        List<GeneExpressionList> lists = new ArrayList<>();
        cells.forEach(c -> {
            ObjectId id = new ObjectId();
            lists.add(new GeneExpressionList(id));
            c.setGeneExpressionId(id);
            loadMap.put(c.getLocalId(), id);

        });
        saveExpressionLists(lists);
        this.loadMap = loadMap;
        this.geneMap = geneMap;
    }

    public void endExpressionLoad() {
        this.loadMap = null;
        this.geneMap = null;
    }

    public void bulkSaveExpressions(Experiment e, List<GeneExpressionDto> expressionList) {
        long start = System.nanoTime();
        // bulk update
        Map<ObjectId, List<GeneExpression>> map = new HashMap<>();
        expressionList.forEach(ex -> {
            List<GeneExpression> list = map.get(loadMap.get(ex.getCellId()));
            if (list == null) {
                list = new ArrayList<>();
                map.put(loadMap.get(ex.getCellId()), list);
            }
            GeneExpression ge = new GeneExpression(geneMap.get(ex.getGeneId()), ex.getExpression());
            list.add(ge);
        });
        // Create a 'updateOne' operation for each list
        // BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
        // GeneExpressionList.class);
        // for (Entry<ObjectId, List<GeneExpression>> entry : map.entrySet()) {
        // ops.updateOne(Query.query(Criteria.where("_id").is(entry.getKey())),
        // new Update().push("geneExpressions").each(entry.getValue()));
        // }
        log.info("Inserting into " + map.values().size() + " gene expression lists");
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
                PartialGeneExpressionList.class);
        List<PartialGeneExpressionList> inserList = new ArrayList<>();
        for (Entry<ObjectId, List<GeneExpression>> entry : map.entrySet()) {
            PartialGeneExpressionList partial = new PartialGeneExpressionList(entry.getKey(), entry.getValue());
            inserList.add(partial);
            // ops.insert(partial);
        }
        ops.insert(inserList);
        ops.execute();
        long end = System.nanoTime();
        log.info("Time spent processing gene expressions: " + (end - start) / 1_000_000_000.0 + " seconds");
        log.info("Ended: " + System.currentTimeMillis());

    }

    public void fillExpressionList(ObjectId id) {
        MatchOperation matchPartial = Aggregation.match(Criteria.where("geneExpressionListId").is(id));
        GroupOperation groupByList = Aggregation.group("geneExpressionListId")
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
                .aggregate(aggregation, "partialGeneExpressionList", ExpressionsResult.class).getUniqueMappedResult();

        if (result != null) {
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)),
                    new Update().set("geneExpressions", result.expressions), "cellExpressionList");
        } else {
            throw new NoObjectFoundException("No partial expressions found for " + id);
        }
    }

    class ExpressionsResult {
        private List<GeneExpression> expressions;
    }
}
