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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dao.input.GeneExpressionMatrixInputDao;
import com.lifescs.singlecell.dto.csv.GeneExpressionDto;
import com.lifescs.singlecell.model.CellExpression;
import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.repository.CellExpressionListRepository;

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
        BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED, CellExpressionList.class);
        for (Entry<ObjectId, List<CellExpression>> entry : map.entrySet()) {
            ops.updateOne(Query.query(Criteria.where("_id").is(entry.getKey())),
                    new Update().push("expressions").each(entry.getValue()));
        }
        ops.execute();
        long end = System.nanoTime();
        log.info("Time spent processing cell expressions: " + (end - start) / 1_000_000_000.0 + " seconds");

    }
}
