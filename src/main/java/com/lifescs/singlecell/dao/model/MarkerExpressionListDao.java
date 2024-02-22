package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.MarkerExpressionList;
import com.lifescs.singlecell.repository.MarkerExpressionListRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MarkerExpressionListDao {
    private MarkerExpressionListRepository repository;

    public void saveExpressionLists(List<MarkerExpressionList> expressionList) {
        repository.saveAll(expressionList);
    }

}
