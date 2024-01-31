package com.lifescs.singlecell.dao.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.model.ExperimentTestDto;
import com.lifescs.singlecell.dto.model.LowDimentionalDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class ExperimentTestDao {
        private MongoTemplate mongoTemplate;

        public List<ExperimentTestDto> getDto() {
                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.lookup("cell", "cells", "_id", "cell_array"),
                                Aggregation.project()
                                                .and("_id").as("experimentId")
                                                .and("cell_array.barcode").as("barcodes")
                                                .and("cell_array.numberOfUMIs").as("umis"));
                return mongoTemplate.aggregate(aggregation, "experiment", ExperimentTestDto.class).getMappedResults();
        }

        public List<LowDimentionalDto> getLowDimentionalDto(Experiment e, List<String> geneCodes) {
                List<ComparisonOperators.Eq> equalsList = new ArrayList<>();
                for (String geneCode : geneCodes) {
                        equalsList.add(ComparisonOperators.Eq.valueOf("geneExp.geneCode")
                                        .equalTo(geneCode));
                }
                ComparisonOperators.Eq[] equalsArray = equalsList.toArray(new ComparisonOperators.Eq[0]);

                Aggregation agg = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("_id").is(e.getId())),
                                Aggregation.unwind("cells"),
                                Aggregation.lookup("cell", "cells", "_id", "cellInfo"),
                                Aggregation.unwind("cellInfo"),
                                Aggregation.lookup("geneExpressionList", "cellInfo.geneExpressions", "_id",
                                                "geneExpressionInfo"),
                                Aggregation.unwind("geneExpressionInfo"),
                                Aggregation.project()
                                                .and("cellInfo._id").as("cellId")
                                                .and("cellInfo.spring1").as("spring1")
                                                .and("cellInfo.spring2").as("spring2")
                                                .and("cellInfo.tsne1").as("tsne1")
                                                .and("cellInfo.tsne2").as("tsne2")
                                                .and(
                                                                ArrayOperators.Filter.filter(
                                                                                "geneExpressionInfo.geneExpressions")
                                                                                .as("geneExp")
                                                                                .by(BooleanOperators.Or
                                                                                                .or(equalsArray)))
                                                .as("geneValues"),
                                Aggregation.project("cellId", "spring1", "spring2", "tsne1", "tsne2")
                                                .and(AccumulatorOperators.Sum.sumOf("geneValues.expression"))
                                                .as("sum_of_genes"),
                                Aggregation.group("_id")
                                                .push("cellId").as("cellIds")
                                                .push("spring1").as("spring1")
                                                .push("spring2").as("spring2")
                                                .push("tsne1").as("tsne1")
                                                .push("tsne2").as("tsne2")
                                                .push("sum_of_genes").as("sum"));

                return mongoTemplate.aggregate(agg, "experiment", LowDimentionalDto.class).getMappedResults();
        }

        public LowDimentionalDto performAggregation(String experimentId, List<String> geneCodes) {
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

                MatchOperation matchExperiment = Aggregation.match(Criteria.where("_id").is(experimentId));

                UnwindOperation unwindCells = Aggregation.unwind("$cells");
                UnwindOperation unwindCellInfo = Aggregation.unwind("$cellInfo");
                UnwindOperation unwindGeneExpressionInfo = Aggregation.unwind("$geneExpressionInfo");

                ProjectionOperation project = Aggregation.project("_id")
                                .and("cellInfo._id").as("cellId")
                                .and("cellInfo.spring1").as("spring1")
                                .and("cellInfo.spring2").as("spring2")
                                .and("cellInfo.tsne1").as("tsne1")
                                .and("cellInfo.tsne2").as("tsne2")
                                .and(ArrayOperators.Filter.filter("geneExpressionInfo.geneExpressions")
                                                .as("geneExp")
                                                .by(ArrayOperators.In.arrayOf(geneCodes)
                                                                .containsValue("geneExp.geneCode")))
                                .as("geneValues");

                ProjectionOperation projectSum = Aggregation.project("_id")
                                .and("cellId").as("cellId")
                                .and("spring1").as("spring1")
                                .and("spring2").as("spring2")
                                .and("tsne1").as("tsne1")
                                .and("tsne2").as("tsne2")
                                .and(AccumulatorOperators.Sum.sumOf("geneValues.expression"))
                                .as("sum_of_genes");

                GroupOperation group = Aggregation.group("_id")
                                .push("cellId").as("cellIds")
                                .push("spring1").as("spring1")
                                .push("spring2").as("spring2")
                                .push("tsne1").as("tsne1")
                                .push("tsne2").as("tsne2")
                                .push("sum_of_genes").as("sum");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchExperiment,
                                unwindCells,
                                lookupCell,
                                unwindCellInfo,
                                lookupGeneExpression,
                                unwindGeneExpressionInfo,
                                project,
                                projectSum,
                                group);

                List<LowDimentionalDto> result = mongoTemplate
                                .aggregate(aggregation, "experiment", LowDimentionalDto.class).getMappedResults();

                // Handle the result as needed
                return result.isEmpty() ? null : result.get(0);
        }
}
