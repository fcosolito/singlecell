package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.model.HeatmapClusterLoadDto;
import com.lifescs.singlecell.model.Cluster;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClusterDao {
    private MongoTemplate mongoTemplate;

    public List<HeatmapClusterLoadDto> getMarkerExpressionsForCluster(Experiment e, Cluster c)
            throws NoObjectFoundException {
        MatchOperation matchExperiment = Aggregation.match(Criteria.where("_id").is(e.getId()));
        UnwindOperation unwindCells = Aggregation.unwind("cells");

        LookupOperation lookupCells = LookupOperation.newLookup()
                .from("cell")
                .localField("cells")
                .foreignField("_id")
                .as("cellInfo");

        UnwindOperation unwindCellInfo = Aggregation.unwind("cellInfo");
        MatchOperation matchCluster = Aggregation.match(Criteria.where("cellInfo.clusterIds").is(c.getId()));

        ProjectionOperation projectExpressions = Aggregation.project()
                .and("cellInfo.barcode").as("barcode")
                .and("cellInfo.markerExpressionIds").as("markerExpressions");

        LookupOperation lookupExpressions = LookupOperation.newLookup()
                .from("markerExpressionList")
                .localField("markerExpressions")
                .foreignField("_id")
                .as("expressionInfo");

        UnwindOperation unwindExpressions = Aggregation.unwind("expressionInfo");
        MatchOperation matchExpression = Aggregation
                .match(Criteria.where("expressionInfo.resolution").is(c.getResolution().getId()));

        ProjectionOperation projectBarcodeAndExpressions = Aggregation.project("barcode")
                .and("expressionInfo.markerExpressions").as("expressions");

        Aggregation aggregation = Aggregation.newAggregation(
                matchExperiment,
                unwindCells,
                lookupCells,
                unwindCellInfo,
                matchCluster,
                projectExpressions,
                lookupExpressions,
                unwindExpressions,
                matchExpression,
                projectBarcodeAndExpressions

        );

        List<HeatmapClusterLoadDto> result = mongoTemplate
                .aggregate(aggregation, "experiment", HeatmapClusterLoadDto.class)
                .getMappedResults();
        if (result.isEmpty())
            throw new NoObjectFoundException("No marker expressions found for cluster: " + c.getId());
        else
            return result;
    }

}
