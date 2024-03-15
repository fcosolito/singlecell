package com.lifescs.singlecell.dao.plot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
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
import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class LowDimensionalDao {
    private MongoTemplate mongoTemplate;
    private CellDao cellDao;

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

        List<Cell> cells = cellDao.findCellsByExperiment(e);
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

}
