package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.stream.Collectors;

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
import com.lifescs.singlecell.dto.model.HeatmapDto;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByGene;
import com.lifescs.singlecell.dto.model.LowDimentionalDtoByResolution;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.mongodb.BasicDBObject;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PlotDao {
        private MongoTemplate mongoTemplate;

        public LowDimentionalDtoByGene getLowDimentionalByGeneCodes(Experiment e, List<String> geneCodes) {
                String experimentId = e.getId();
                LookupOperation lookupCell = LookupOperation.newLookup()
                                .from("cell")
                                .localField("cells")
                                .foreignField("_id")
                                .as("cellInfo");

                LookupOperation lookupGeneExpression = LookupOperation.newLookup()
                                .from("geneExpressionList")
                                .localField("cellInfo.geneExpressions")
                                .foreignField("_id")
                                .as("geneExpressionInfo");

                LookupOperation lookupSample = LookupOperation.newLookup()
                                .from("sample")
                                .localField("cellInfo.sample")
                                .foreignField("_id")
                                .as("sampleInfo");

                MatchOperation matchExperiment = Aggregation.match(Criteria.where("_id").is(experimentId));

                UnwindOperation unwindCells = Aggregation.unwind("$cells");
                UnwindOperation unwindCellInfo = Aggregation.unwind("$cellInfo");
                UnwindOperation unwindGeneExpressionInfo = Aggregation.unwind("$geneExpressionInfo");
                UnwindOperation unwindSamples = Aggregation.unwind("$sampleInfo");

                ProjectionOperation project = Aggregation.project("_id")
                                .and("cellInfo.barcode").as("barcodes")
                                .and("sampleInfo.name").as("sampleName")
                                .and("cellInfo.spring1").as("spring1")
                                .and("cellInfo.spring2").as("spring2")
                                .and("cellInfo.tsne1").as("tsne1")
                                .and("cellInfo.tsne2").as("tsne2")
                                .and("cellInfo.umap1").as("umap1")
                                .and("cellInfo.umap2").as("umap2")
                                .and("cellInfo.pca1").as("pca1")
                                .and("cellInfo.pca2").as("pca2")
                                .and(ArrayOperators.Filter.filter("geneExpressionInfo.geneExpressions")
                                                .as("geneExp")
                                                .by(ArrayOperators.In.arrayOf(geneCodes)
                                                                .containsValue("$$geneExp.geneCode")))
                                .as("geneValues");

                ProjectionOperation projectSum = Aggregation.project("_id")
                                .and("barcodes").as("barcode")
                                .and("sampleName").as("sampleName")
                                .and("spring1").as("spring1")
                                .and("spring2").as("spring2")
                                .and("tsne1").as("tsne1")
                                .and("tsne2").as("tsne2")
                                .and("umap1").as("umap1")
                                .and("umap2").as("umap2")
                                .and("pca1").as("pca1")
                                .and("pca2").as("pca2")
                                .and(AccumulatorOperators.Sum.sumOf("geneValues.expression"))
                                .as("sum_of_genes");

                // These aliases must match the attribute names of the dto
                GroupOperation group = Aggregation.group("_id")
                                .push("barcode").as("barcodes")
                                .push("sampleName").as("samples")
                                .push("spring1").as("spring1")
                                .push("spring2").as("spring2")
                                .push("tsne1").as("tsne1")
                                .push("tsne2").as("tsne2")
                                .push("umap1").as("umap1")
                                .push("umap2").as("umap2")
                                .push("pca1").as("pca1")
                                .push("pca2").as("pca2")
                                .push("sum_of_genes").as("expressionSum");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchExperiment,
                                unwindCells,
                                lookupCell,
                                unwindCellInfo,
                                lookupGeneExpression,
                                unwindGeneExpressionInfo,
                                lookupSample,
                                unwindSamples,
                                project,
                                projectSum,
                                group);

                List<LowDimentionalDtoByGene> result = mongoTemplate
                                .aggregate(aggregation, "experiment", LowDimentionalDtoByGene.class).getMappedResults();

                LowDimentionalDtoByGene resultWithAverage = result.isEmpty() ? null : result.get(0);
                if (resultWithAverage != null) {
                        resultWithAverage.setExpressionSum(resultWithAverage.getExpressionSum().stream()
                                        .map(d -> d / geneCodes.size()).collect(Collectors.toList()));
                }
                return resultWithAverage;
        }

        public LowDimentionalDtoByResolution getLowDimentionalDtoByResolution(String experimentId) {

                MatchOperation matchExperiment = Aggregation.match(Criteria.where("_id").is(experimentId));
                UnwindOperation unwindCells = Aggregation.unwind("$cells");
                UnwindOperation unwindCellinfo = Aggregation.unwind("$cellInfo");
                UnwindOperation unwindClusterInfo = Aggregation.unwind("$clusterInfo");
                UnwindOperation unwindResolutionInfo = Aggregation.unwind("$resolutionInfo");

                LookupOperation lookupCell = LookupOperation.newLookup()
                                .from("cell")
                                .localField("cells")
                                .foreignField("_id")
                                .as("cellInfo");

                LookupOperation lookupCluster = LookupOperation.newLookup()
                                .from("cluster")
                                .localField("cellInfo.clusters")
                                .foreignField("_id")
                                .as("clusterInfo");

                LookupOperation lookupResolution = LookupOperation.newLookup()
                                .from("resolution")
                                .localField("clusterInfo.resolution")
                                .foreignField("_id")
                                .as("resolutionInfo");

                ProjectionOperation projectByCell = Aggregation.project()
                                .and("cellInfo.barcode").as("barcode")
                                .and("cellInfo.spring1").as("spring1")
                                .and("cellInfo.spring2").as("spring2")
                                .and("cellInfo.umap1").as("umap1")
                                .and("cellInfo.umap2").as("umap2")
                                .and("cellInfo.pca1").as("pca1")
                                .and("cellInfo.pca2").as("pca2")
                                .and("cellInfo.tsne1").as("tsne1")
                                .and("cellInfo.tsne2").as("tsne2")
                                .and("resolutionInfo.name").as("resolutionName")
                                .and("clusterInfo.name").as("clusterName");

                ProjectionOperation projectByExperiment = Aggregation.project()
                                .and("AuxId").asLiteral().as("experimentId")
                                .and("spring1").as("spring1")
                                .and("spring2").as("spring2")
                                .and("umap1").as("umap1")
                                .and("umap2").as("umap2")
                                .and("pca1").as("pca1")
                                .and("pca2").as("pca2")
                                .and("tsne1").as("tsne1")
                                .and("tsne2").as("tsne2")
                                .and("_id").as("barcode")
                                .and("resolutions").as("resolutions");

                GroupOperation groupByCell = Aggregation.group("barcode")
                                .first("spring1").as("spring1")
                                .first("spring2").as("spring2")
                                .first("umap1").as("umap1")
                                .first("umap2").as("umap2")
                                .first("pca1").as("pca1")
                                .first("pca2").as("pca2")
                                .first("tsne1").as("tsne1")
                                .first("tsne2").as("tsne2")
                                .push(
                                                new BasicDBObject("resolutionName", "$resolutionName")
                                                                .append("clusterName", "$clusterName"))
                                .as("resolutions");

                GroupOperation groupByExperiment = Aggregation.group("experimentId")
                                .push("barcode").as("barcodes")
                                .push("spring1").as("spring1")
                                .push("spring2").as("spring2")
                                .push("umap1").as("umap1")
                                .push("umap2").as("umap2")
                                .push("pca1").as("pca1")
                                .push("pca2").as("pca2")
                                .push("tsne1").as("tsne1")
                                .push("tsne2").as("tsne2")
                                .push("resolutions").as("resolutions");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchExperiment,
                                unwindCells,
                                lookupCell,
                                unwindCellinfo,
                                lookupCluster,
                                unwindClusterInfo,
                                lookupResolution,
                                unwindResolutionInfo,
                                projectByCell,
                                groupByCell,
                                projectByExperiment,
                                groupByExperiment);

                List<LowDimentionalDtoByResolution> result = mongoTemplate
                                .aggregate(aggregation, "experiment", LowDimentionalDtoByResolution.class)
                                .getMappedResults();

                return result.isEmpty() ? null : result.get(0);

                // for each cell get the clusters -> get their resolutions -> do not unwind so
                // each cell has an array of resolutions, project only the resolution name in
                // the
                // nested object and find a way to get the cluster name inside the resolution
                // object
                // group by cell and push each resolution object into an array so:
                // resolutions: [{ resName: "aaa", clusName: "bbb" }, {...}]
        }

        public LowDimentionalDtoByResolution getLowDimentionalDtoByResolution2(Experiment e, Resolution r) {
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
                                .localField("cellInfo.clusters")
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
                List<LowDimentionalDtoByResolution> result = mongoTemplate
                                .aggregate(aggregation, "experiment", LowDimentionalDtoByResolution.class)
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
}
