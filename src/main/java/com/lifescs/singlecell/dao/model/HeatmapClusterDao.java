package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.HeatmapCluster;
import com.lifescs.singlecell.repository.HeatmapClusterRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class HeatmapClusterDao {
    private HeatmapClusterRepository repository;

    public void saveAll(List<HeatmapCluster> list) {
        repository.saveAll(list);
    }

}
