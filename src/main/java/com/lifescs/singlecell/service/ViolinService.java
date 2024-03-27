package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.ViolinDao;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ViolinService {
  private ViolinDao violinDao;

  public List<ViolinDto> getDtosByResolution(Experiment e, List<String> codes, Resolution r){
    return violinDao.getDtosByResolution(e, codes, r);
  }
}
