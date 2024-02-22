package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.CellDao;
import com.lifescs.singlecell.model.Cell;
import com.lifescs.singlecell.model.Cluster;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CellService {
    private CellDao cellDao;

    public List<Cluster> getCellClusters(Cell c) {
        return cellDao.getCellClusters(c);
    }

}
