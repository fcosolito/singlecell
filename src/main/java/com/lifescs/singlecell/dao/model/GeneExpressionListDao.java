package com.lifescs.singlecell.dao.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.GeneExpressionList;
import com.lifescs.singlecell.repository.GeneExpressionListRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GeneExpressionListDao {
    private GeneExpressionListRepository repository;

    public void saveExpressionLists(List<GeneExpressionList> list) {
        repository.saveAll(list);
    }

}
