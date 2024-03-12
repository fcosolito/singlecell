package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.dto.query.ViolinQueryDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpression;
import com.lifescs.singlecell.model.Resolution;
import com.mongodb.BasicDBObject;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class PlotDao {
        private MongoTemplate mongoTemplate;

        class ExpressionSumResult {
                private String id;
                private Double sumOfExpressions;
        }

        public LowDimensionalDto getLowDimensionalDtoByGeneCodes(Experiment e, List<String> geneCodes) {
                MatchOperation matchGenes = Aggregation.match(Criteria.where("experimentId").is(e.getId())
                                .and("geneCode").in(geneCodes));
                UnwindOperation unwindExpressions = Aggregation.unwind("expressions");

                GroupOperation groupByCell = Aggregation.group("expressions.cellId")
                                .sum("expressions.expression")
                                .as("sumOfExpressions");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchGenes,
                                unwindExpressions,
                                groupByCell);

                List<ExpressionSumResult> expressionSums = mongoTemplate
                                .aggregate(aggregation, "cellExpressionList", ExpressionSumResult.class)
                                .getMappedResults();
                if (expressionSums.isEmpty())
                        throw new NoObjectFoundException(
                                        "Could not get expression sums for gene codes: " + geneCodes.toString());

                LowDimensionalDto result = new LowDimensionalDto();

                //List<Cell> cells = e.getCells();
                // TODO QUERY experiment cells
                List<Cell> cells = null;
                Map<String, Double> expressionSumsMap = expressionSums.stream()
                                .collect(Collectors.toMap(es -> es.id, es -> es.sumOfExpressions));
                cells.stream().forEach(cell -> {
                        Double expression = 0.0;
                        /*
                         * Optional<Double> expressionSumOpt = expressionSums.stream()
                         * .filter(c -> c.id.equals(cell.getId())).map(c -> c.sumOfExpressions)
                         * .findFirst();
                         * if (expressionSumOpt.isPresent())
                         * expression = expressionSumOpt.get() / geneCodes.size();
                         */
                        Double expOpt = expressionSumsMap.get(cell.getId());
                        if (expOpt != null)
                                expression = expOpt;

                        result.getBarcodes().add(cell.getBarcode());
                        result.getSpring1().add(cell.getSpring1());
                        result.getSpring2().add(cell.getSpring2());
                        result.getPca1().add(cell.getPca1());
                        result.getPca2().add(cell.getPca2());
                        result.getUmap1().add(cell.getUmap1());
                        result.getUmap2().add(cell.getUmap2());
                        result.getTsne1().add(cell.getTsne1());
                        result.getTsne2().add(cell.getTsne2());
                        result.getSamples().add(cell.getSample().getName());
                        result.getExpressionSum().add(expression);
                });

                return result;
        }

        public LowDimensionalDto getLowDimensionalDtoByResolution2(Experiment e, Resolution r) {
                MatchOperation matchExperiment = Aggregation.match(Criteria.where("_id").is(e.getId()));
                UnwindOperation unwindCells = Aggregation.unwind("$cells");
                UnwindOperation unwindCellInfo = Aggregation.unwind("$cellInfo");
                UnwindOperation unwindClusterInfo = Aggregation.unwind("$clusterInfo");

                LookupOperation lookupCell = LookupOperation.newLookup()
                                .from("cell")
                                .localField("cells")
                                .foreignField("_id")
                                .as("cellInfo");

                LookupOperation lookupCluster = LookupOperation.newLookup()
                                .from("cluster")
                                .localField("cellInfo.clusterIds")
                                .foreignField("_id")
                                .as("clusterInfo");

                GroupOperation groupByExperiment = Aggregation.group("_id")
                                .push("barcode").as("barcodes")
                                .push("spring1").as("spring1")
                                .push("spring2").as("spring2")
                                .push("umap1").as("umap1")
                                .push("umap2").as("umap2")
                                .push("pca1").as("pca1")
                                .push("pca2").as("pca2")
                                .push("tsne1").as("tsne1")
                                .push("tsne2").as("tsne2")
                                .push("clusterInfo.name").as("clusterNames");

                // ERROR: if the resolution or cluster selected is missing for some cells the
                // whole dto would
                // display wrong data or could make the frontend break. So the filter must
                // always return an element
                ProjectionOperation projectFilterCluster = Aggregation.project("_id")
                                .and("cellInfo.barcode").as("barcode")
                                .and("cellInfo.spring1").as("spring1")
                                .and("cellInfo.spring2").as("spring2")
                                .and("cellInfo.umap1").as("umap1")
                                .and("cellInfo.umap2").as("umap2")
                                .and("cellInfo.pca1").as("pca1")
                                .and("cellInfo.pca2").as("pca2")
                                .and("cellInfo.tsne1").as("tsne1")
                                .and("cellInfo.tsne2").as("tsne2")
                                .and(ArrayOperators.Filter.filter("clusterInfo")
                                                .as("cluster")
                                                .by(ComparisonOperators.Eq.valueOf("cluster.resolution")
                                                                .equalToValue(r.getId())))
                                .as("clusterInfo");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchExperiment,
                                unwindCells,
                                lookupCell,
                                unwindCellInfo,
                                lookupCluster,
                                projectFilterCluster, // only one cluster per cell left
                                unwindClusterInfo,
                                groupByExperiment

                );
                List<LowDimensionalDto> result = mongoTemplate
                                .aggregate(aggregation, "experiment", LowDimensionalDto.class)
                                .getMappedResults();

                return result.isEmpty() ? null : result.get(0);

        }

        public List<HeatmapDto> getHeatmapDtos(Resolution r) {
                MatchOperation matchCluster = Aggregation.match(Criteria.where("resolution").is(r.getId()));

                LookupOperation lookupHeatmapCluster = LookupOperation.newLookup()
                                .from("heatmapCluster")
                                .localField("heatmapClusterId")
                                .foreignField("_id")
                                .as("heatmapInfo");

                ProjectionOperation project = Aggregation.project("name")
                                .and("heatmapInfo.buckets").as("buckets")
                                .and("heatmapInfo.topMarkers").as("markers")
                                .and("heatmapInfo.expressions").as("expressions");

                UnwindOperation unwindExpressions = Aggregation.unwind("$expressions");
                UnwindOperation unwindBuckets = Aggregation.unwind("$buckets");
                UnwindOperation unwindMarkers = Aggregation.unwind("$markers");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchCluster,
                                lookupHeatmapCluster,
                                project,
                                unwindMarkers,
                                unwindExpressions,
                                unwindBuckets);

                List<HeatmapDto> result = mongoTemplate
                                .aggregate(aggregation, "cluster", HeatmapDto.class)
                                .getMappedResults();

                if (result.isEmpty())
                        throw new NoObjectFoundException("No heatmap clusters found for resolution: " + r.getId());
                else
                        return result;
        }

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
                        List<ViolinDto> resultDtos = result.stream().map(dto -> {
                                ViolinDto resultDto = new ViolinDto();
                                resultDto.setClusterId(dto.getClusterId());
                                resultDto.setSampleId(dto.getSampleId());
                                dto.getExpressionLists().stream().forEach(cell -> {
                                        geneCodes.stream().forEach(gc -> {
                                                Optional<GeneExpression> expressionOpt = cell.stream()
                                                                .filter(ex -> ex.getGeneCode().equals(gc)).findFirst();
                                                if (expressionOpt.isPresent()) {
                                                        resultDto.getExpressions().add(expressionOpt.get());
                                                } else {
                                                        resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
                                                }
                                        });
                                });
                                for (int i = 0; i < (dto.getCellCount() - dto.getExpressionLists().size()); i++) {
                                        geneCodes.stream().forEach(gc -> {
                                                resultDto.getExpressions().add(new GeneExpression(gc, 0.0));
                                        });
                                }
                                return resultDto;
                        }).toList();
                        log.info("Completed query in " + (end - start) / 1_000_000_000.0 + " seconds");
                        */
                        //return resultDtos;
                        return null;

                }
        }
}
