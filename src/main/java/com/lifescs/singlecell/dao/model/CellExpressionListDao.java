package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.CellExpressionList;
import com.lifescs.singlecell.repository.CellExpressionListRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CellExpressionListDao {
    private CellExpressionListRepository repository;

    public void saveExpressionLists(List<CellExpressionList> expressions) {
        repository.saveAll(expressions);
    }

}
