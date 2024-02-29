package com.lifescs.singlecell.dao.model;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.Exceptions.NoObjectFoundException;
import com.lifescs.singlecell.dto.api.TopMarkerDto;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.repository.ResolutionRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ResolutionDao {
    private MongoTemplate mongoTemplate;
    private ResolutionRepository resolutionRepository;

    public Optional<Resolution> findResolutionById(String id) {
        return resolutionRepository.findById(id);
    }

    public List<TopMarkerDto> getTopMarkers(Resolution r, Integer markersPerCluster) {
        MatchOperation matchClusters = Aggregation.match(Criteria.where("resolution").is(r.getId()));

        ProjectionOperation projectSlice = Aggregation.project()
                .and("_id").as("clusterId")
                .and("markers.geneCode").slice(markersPerCluster).as("topMarkers");

        Aggregation aggregation = Aggregation.newAggregation(
                matchClusters,
                projectSlice);

        List<TopMarkerDto> result = mongoTemplate.aggregate(aggregation, "cluster", TopMarkerDto.class)
                .getMappedResults();
        if (result.isEmpty())
            throw new NoObjectFoundException("No results for top markers in resolution: " + r.getId());
        else
            return result;
    }
}
