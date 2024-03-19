package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsDontMatch;
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsMatch;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.input.GeneExpressionDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.CellExpression;
import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.PartialCellExpressionList;
import com.lifescs.singlecell.repository.CellExpressionListRepository;
import com.mongodb.client.result.DeleteResult;

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

  public void save(CellExpressionList list) {
    repository.save(list);
  }

  public List<CellExpressionList> findListsByExperiment(Experiment e) {
    LookupOperation lookupCell = Aggregation.lookup(
        "cell",
        "cell.$id",
        "_id",
        "cellInfo");

    MatchOperation matchExperiment = Aggregation.match(
        Criteria.where("cellInfo.experiment.$id").is(e.getId()));

    Aggregation aggregation = Aggregation.newAggregation(
        lookupCell,
        matchExperiment);

    List<CellExpressionList> result = mongoTemplate
        .aggregate(aggregation, "cellExpressionList", CellExpressionList.class)
        .getMappedResults();
    if (result.isEmpty())
      throw new NoObjectFoundException("No cell expression lists found for experiment: " + e.getId());
    else
      return result;

  }

  // Must be executed before removing cells
  // should be called in CellDao.deleteCellsByExperiment method
  // causes heap space to explode
  public void deleteListsByExperiment(Experiment e) {
    repository.deleteAll(findListsByExperiment(e));
  }

  /*
  public void startExpressionLoad(Experiment e, Map<Integer, String> geneMap) {
    this.inputId2Partial = new HashMap<>();

    // Get cells for this experiment
    List<Cell> cells = cellDao.findCellsByExperiment(e);

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
  */

  public void deleteListByCell(Cell cell) {
    Query query = Query.query(Criteria.where("cell.$id").is(new ObjectId(cell.getId())));
    mongoTemplate.updateFirst(query, Update.update("deleted", true), "cellExpressionList");
  }

public void clean() {
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "cellExpressionList");
}

  /*
   * public void bulkSaveExpressions(Experiment e, List<GeneExpressionDto>
   * expressionList) {
   * // bulk update
   * Map<Integer, PartialCellExpressionList> insertMap = new HashMap<>();
   * for (GeneExpressionDto ex : expressionList) {
   * PartialCellExpressionList partial = insertMap.get(ex.getCellId());
   * if (partial == null) {
   * partial = inputId2Partial.get(ex.getCellId());
   * if (partial == null)
   * throw new RuntimeException("Could not get partial for cell: " +
   * ex.getCellId());
   * }
   * 
   * insertMap.put(ex.getCellId(), partial);
   * CellExpression ce = new CellExpression(inputId2Code.get(ex.getGeneId()),
   * ex.getExpression());
   * partial.getExpressions().add(ce);
   * }
   * List<PartialCellExpressionList> insertList =
   * insertMap.values().stream().toList();
   * log.info("List size: " + insertList.size());
   * 
   * BulkOperations ops = mongoTemplate.bulkOps(BulkMode.UNORDERED,
   * PartialCellExpressionList.class);
   * 
   * ops.insert(insertList);
   * ops.execute();
   * insertList = null;
   * insertMap = null;
   * }
   * 
   * public void fillExpressionLists() {
   * log.info("Filling cell expression lists");
   * GroupOperation groupPartials = Aggregation.group("cell")
   * .push("expressions").as("expressions");
   * 
   * ProjectionOperation projectLists = Aggregation.project()
   * .and("_id").as("cell")
   * .and(ArrayOperators.Reduce.arrayOf("expressions").withInitialValue(new
   * ArrayList<>())
   * .reduce(ArrayOperators.ConcatArrays.arrayOf("$$value")
   * .concat("$$this")))
   * .as("expressions");
   * 
   * MergeOperation merge =
   * Aggregation.merge().intoCollection("cellExpressionList")
   * .on("cell").whenMatched(WhenDocumentsMatch.failOnMatch())
   * .whenDocumentsDontMatch(WhenDocumentsDontMatch.insertNewDocument()).build();
   * 
   * Aggregation aggregation = Aggregation.newAggregation(
   * groupPartials,
   * projectLists,
   * merge)
   * .withOptions(AggregationOptions.builder().allowDiskUse(true).skipOutput().
   * build());
   * 
   * mongoTemplate.aggregate(aggregation, "partialCellExpressionList",
   * Document.class);
   * }
   */
}
