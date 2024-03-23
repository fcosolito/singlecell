package com.lifescs.singlecell.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.LowDimensionalDao;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class LowDimensionalService {
  private LowDimensionalDao lowDimensionalDao;

  public LowDimensionalDto getLowDimensionalByGene(Experiment e, List<String> codes) {
    long start = System.nanoTime();
    Map<String, Double> barcode2expressionSum = lowDimensionalDao.getExpressionSumMap(e, codes);
    LowDimensionalDto dto = lowDimensionalDao.getDtoBySamples(e);
    Integer numberOfGenes = codes.size();
    dto.getBarcodes().stream().forEach(
      barcode -> dto.getExpressionAvg().add(barcode2expressionSum.get(barcode)/numberOfGenes));
    long end = System.nanoTime();
    log.info("Get low dimensional by genes: " + (end-start)/1_000_000_000.0 + " seconds");

    return dto;
  }

  public LowDimensionalDto getLowDimensionalBySamples(Experiment e){
    return lowDimensionalDao.getDtoBySamples(e);
  }

  public LowDimensionalDto getLowDimensionalByResolution(Experiment e, Resolution r){
    return lowDimensionalDao.getDtoByResolution(e, r);
  }

}
