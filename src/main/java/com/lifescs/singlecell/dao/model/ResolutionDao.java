package com.lifescs.singlecell.dao.model;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

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
}
