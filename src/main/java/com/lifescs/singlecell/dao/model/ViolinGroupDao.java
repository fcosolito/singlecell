package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.ViolinGroup;
import com.lifescs.singlecell.repository.ViolinGroupRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ViolinGroupDao {
  private MongoTemplate mongoTemplate;
  private ViolinGroupRepository repository;

  public void saveAll(List<ViolinGroup> groups){
    repository.saveAll(groups);
  }
}
