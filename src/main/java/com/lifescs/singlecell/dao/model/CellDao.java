package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.query.HeatmapClusterLoadDto;
import com.lifescs.singlecell.dto.query.ViolinGroupLoadDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.CellRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CellDao {

  private MongoTemplate mongoTemplate;
  private CellRepository cellRepository;
  private CellExpressionListDao cellExpressionListDao;

  public Optional<Cell> findCellById(String id) {
    return cellRepository.findById(id);
  }

  public void saveCells(List<Cell> cl) {
    cellRepository.saveAll(cl);
  }

  public void deleteCell(Cell cell) {
    cellExpressionListDao.deleteListByCell(cell);
    Query query = Query.query(Criteria.where("_id").is(cell.getId()));
    mongoTemplate.updateFirst(query, Update.update("deleted", true), "cell");
  }

  public void deleteCellsByExperiment(Experiment e) {
    findCellsByExperiment(e).stream().forEach(cell -> deleteCell(cell));
    log.info("Deleted cells");
  }

  public List<Cell> findCellsByExperiment(Experiment e) {
    log.info("Finding cells");
    // Try to get cells from loaded experiment
    List<Cell> cells;
    if (e.getCells() != null)
      cells = e.getCells();
    else {
      log.info("Executing query");
      Query query = Query.query(Criteria.where("experiment.$id").is(e.getId()));
      cells = mongoTemplate.find(query, Cell.class);
    }

    if (cells == null | cells.isEmpty())
      throw new NoObjectFoundException("No cells for experiment: " + e.getId());
    else {
      e.setCells(cells);
      return cells;
    }
  }

  public List<Cluster> getCellClusters(Cell c) throws NoObjectFoundException {
    MatchOperation matchCell = Aggregation.match(Criteria.where("_id").is(c.getId()));
    UnwindOperation unwindCluster = Aggregation.unwind("clusters");
    UnwindOperation unwindClusterInfo = Aggregation.unwind("clusterInfo");

    LookupOperation lookupCluster = LookupOperation.newLookup()
        .from("cluster")
        .localField("clusters")
        .foreignField("_id")
        .as("clusterInfo");

    ProjectionOperation projectCluster = Aggregation.project("clusterInfo");

    Aggregation aggregation = Aggregation.newAggregation(
        matchCell,
        unwindCluster,
        lookupCluster,
        projectCluster,
        unwindClusterInfo);
    List<ClusterResult> result = mongoTemplate.aggregate(aggregation, "cell", ClusterResult.class)
        .getMappedResults();
    if (result.isEmpty())
      throw new NoObjectFoundException("No cluster was found for cell with cell id: " + c.getId());
    else
      return result.stream().map(r -> r.cluster).toList();
  }

  class ClusterResult {
    private Cluster cluster;
  }

  public void clean() {
    Query query = Query.query(Criteria.where("deleted").is(true));
    mongoTemplate.remove(query, "cell");
    cellExpressionListDao.clean();
  }

  public List<HeatmapClusterLoadDto> getMarkerExpressionsByCellIds(List<ObjectId> cellIds,
      List<String> geneCodes)
      throws NoObjectFoundException {

    LookupOperation lookupCellExpressions = LookupOperation.newLookup()
        .from("cellExpressionList")
        .localField("_id")
        .foreignField("cell.$id")
        .as("expressionInfo");

    MatchOperation matchCells = Aggregation.match(Criteria.where("_id")
        .in(cellIds));

    UnwindOperation unwindExpressions = Aggregation.unwind("expressionInfo");

    ProjectionOperation projectBarcodeAndExpressions = Aggregation.project()
        .and("barcode").as("barcode")
        .and(ArrayOperators.Filter.filter("expressionInfo.expressions")
            .as("expression")
            .by(ArrayOperators.In.arrayOf(geneCodes)
                .containsValue("$$expression.code")))
        .as("expressions");

    Aggregation aggregation = Aggregation.newAggregation(
        matchCells,
        lookupCellExpressions,
        unwindExpressions,
        projectBarcodeAndExpressions

    );

    List<HeatmapClusterLoadDto> result = mongoTemplate
        .aggregate(aggregation, "cell", HeatmapClusterLoadDto.class)
        .getMappedResults();
    return result;
  }

  public List<ObjectId> getCellIdsByCluster(Cluster cluster) {
    MatchOperation matchCells = Aggregation.match(Criteria.where("cellClusters")
        .elemMatch(Criteria.where("cluster.$id").is(new ObjectId(cluster.getId()))));

    ProjectionOperation projectIds = Aggregation.project("_id");

    Aggregation aggregation = Aggregation.newAggregation(
        matchCells,
        projectIds);
    List<ObjectId> result = mongoTemplate.aggregate(aggregation, "cell", CellId.class).getMappedResults().stream()
        .map(cellId -> cellId.id).toList();
    if (result.isEmpty())
      throw new NoObjectFoundException("No cell ids for cluster: " + cluster.getName());
    return result;
  }

  class CellId {
    ObjectId id;
  }

  public List<ViolinGroupLoadDto> getViolinGroupLoadDtos(Experiment e, Resolution r) {
    MatchOperation matchExperiment = Aggregation.match(Criteria.where("experiment.$id").is(e.getId()));
    UnwindOperation unwindCellClusters = Aggregation.unwind("cellClusters");
    MatchOperation matchResolution = Aggregation.match(Criteria.where("cellClusters.resolution.$id").is(new ObjectId(r.getId())));
    ProjectionOperation project = Aggregation.project()
    .and("_id").as("cell")
    .andExpression("cellClusters.cluster.$id").as("cluster")
    .andExpression("sample.$id").as("sample");

    Aggregation aggregation = Aggregation.newAggregation(
      matchExperiment,
      unwindCellClusters,
      matchResolution,
      project 
    );

    List<ViolinGroupLoadDto> result = mongoTemplate.aggregate(aggregation, "cell", ViolinGroupLoadDto.class).getMappedResults();
    Document docs = mongoTemplate.aggregate(aggregation, "cell", ViolinGroupLoadDto.class).getRawResults();
    //log.info("Documents: " + docs);
    //log.info("Load dtos: " + result.stream());
    return result;
  }
}
