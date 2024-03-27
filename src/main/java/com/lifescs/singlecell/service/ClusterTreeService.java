package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.ClusterTreeDao;
import com.lifescs.singlecell.dto.api.ClusterTreeDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ClusterTreeService {
  private ClusterTreeDao clusterTreeDao;

  public List<ClusterTreeDto> getDtos(Experiment experiment){
    return clusterTreeDao.getDtos(experiment);
  }
  
}
