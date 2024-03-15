package com.lifescs.singlecell.dao.plot;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.dto.query.ViolinQueryDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.mongodb.BasicDBObject;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ViolinDao {
    private MongoTemplate mongoTemplate;

    public List<ViolinDto> getViolinDtos(Experiment e, Resolution r, List<String> geneCodes) {
        MatchOperation matchCellExpression = Aggregation.match(Criteria.where("experimentId").is(e.getId())
                .and("geneCode").in(geneCodes));
        UnwindOperation unwindExpressions = Aggregation.unwind("$expressions");

        GroupOperation groupByCell = Aggregation.group("expressions.cellId")
                .push(new BasicDBObject("geneCode", "$geneCode")
                        .append("expression", "$expressions.expression"))
                .as("expressions");

        LookupOperation lookupCells = LookupOperation.newLookup()
                .from("cell")
                .localField("_id")
                .foreignField("_id")
                .as("cellInfo");

        LookupOperation lookupClusters = LookupOperation.newLookup()
                .from("cluster")
                .localField("cellInfo.clusterIds")
                .foreignField("_id")
                .as("clusterInfo");

        UnwindOperation unwindClusterInfo = Aggregation.unwind("$clusterInfo");

        MatchOperation matchCluster = Aggregation.match(Criteria.where("clusterInfo.resolution").is(r.getId()));

        ProjectionOperation projectIds = Aggregation.project("expressions")
                .and("clusterInfo._id").as("clusterId")
                .and("cellInfo.sample").as("sampleId");

        GroupOperation groupByClusterAndSample = Aggregation.group("clusterId", "sampleId")
                .push("expressions").as("expressions");

        LookupOperation lookupCellCount = LookupOperation.newLookup()
                .from("cell")
                .localField("_id.clusterId")
                .foreignField("clusterIds")
                .pipeline(Aggregation.group()
                        .count().as("cellCount"))
                .as("cellCount");

        ProjectionOperation projectCellCount = Aggregation.project()
                .and("expressions").as("expressionLists")
                .and("cellCount.cellCount").as("cellCount")
                .and("_id.clusterId").as("clusterId")
                .and("_id.sampleId").as("sampleId");

        UnwindOperation unwindSampleId = Aggregation.unwind("$sampleId");
        UnwindOperation unwindCellCount = Aggregation.unwind("$cellCount");

        Aggregation aggregation = Aggregation.newAggregation(
                matchCellExpression,
                unwindExpressions,
                groupByCell,
                lookupCells,
                lookupClusters,
                unwindClusterInfo,
                matchCluster,
                projectIds,
                groupByClusterAndSample,
                lookupCellCount,
                projectCellCount,
                unwindSampleId,
                unwindCellCount);

        long start = System.nanoTime();
        List<ViolinQueryDto> result = mongoTemplate
                .aggregate(aggregation, "cellExpressionList", ViolinQueryDto.class)
                .getMappedResults();
        long end = System.nanoTime();
        if (result.isEmpty())
            throw new NoObjectFoundException("Could not get violin dtos for experiment: " + e.getId());
        else {
            /*
             * List<ViolinDto> resultDtos = result.stream().map(dto -> {
             * ViolinDto resultDto = new ViolinDto();
             * resultDto.setClusterId(dto.getClusterId());
             * resultDto.setSampleId(dto.getSampleId());
             * dto.getExpressionLists().stream().forEach(cell -> {
             * geneCodes.stream().forEach(gc -> {
             * Optional<GeneExpression> expressionOpt = cell.stream()
             * .filter(ex -> ex.getGeneCode().equals(gc)).findFirst();
             * if (expressionOpt.isPresent()) {
             * resultDto.getExpressions().add(expressionOpt.get());
             * } else {
             * resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
             * }
             * });
             * });
             * for (int i = 0; i < (dto.getCellCount() - dto.getExpressionLists().size());
             * i++) {
             * geneCodes.stream().forEach(gc -> {
             * resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
             * });
             * }
             * return resultDto;
             * }).toList();
             * log.info("Completed query in " + (end - start) / 1_000_000_000.0 +
             * " seconds");
             */
            // return resultDtos;
            return null;

        }

    }
}
