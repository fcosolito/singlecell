package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.repository.CellRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CellDao {

    private MongoTemplate mongoTemplate;
    private CellRepository cellRepository;

    public Optional<Cell> findCellById(String id) {
        return cellRepository.findById(id);
    }

    public void saveCells(List<Cell> cl) {
        cellRepository.saveAll(cl);
    }

    public void deleteCellsByExperiment(Experiment e) {
        Query query = Query.query(Criteria.where("experiment.$id").is(e.getId()));
        mongoTemplate.remove(query, Cell.class);
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
}
