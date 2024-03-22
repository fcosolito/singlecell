package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.HeatmapDao;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HeatmapService {
  private HeatmapDao heatmapDao;

  public List<HeatmapDto> getHeatmapByResolution(Resolution resolution){
    return heatmapDao.getHeatmapDtos2(resolution);
  }

}
