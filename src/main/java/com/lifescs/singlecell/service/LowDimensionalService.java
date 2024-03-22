package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.LowDimensionalDao;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Experiment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class LowDimensionalService {
  private LowDimensionalDao lowDimensionalDao;

public LowDimensionalDto getLowDimensionalByGene(Experiment e, List<String> codes) {
    return lowDimensionalDao.getDtoByGenes(e, codes);
}


}
