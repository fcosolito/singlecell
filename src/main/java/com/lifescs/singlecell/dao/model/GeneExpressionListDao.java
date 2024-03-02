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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.GeneExpressionList;
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
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED, GeneExpressionList.class);
        for (Entry<ObjectId, List<GeneExpression>> entry : map.entrySet()) {
            ops.updateOne(Query.query(Criteria.where("_id").is(entry.getKey())),
                    new Update().push("geneExpressions").each(entry.getValue()));
        }
        ops.execute();
        long end = System.nanoTime();
        log.info("Time spent processing gene expressions: " + (end - start) / 1_000_000_000.0 + " seconds");

    }
}
