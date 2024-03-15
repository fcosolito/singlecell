package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.HeatmapDao;
import com.lifescs.singlecell.dao.plot.LowDimensionalDao;
import com.lifescs.singlecell.dao.plot.ViolinDao;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.api.LowDimensionalDto;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PlotService {
    private LowDimensionalDao lowDimensionalDao;
    private HeatmapDao heatmapDao;
    private ViolinDao violinDao;

    public LowDimensionalDto getLowDimensionalDtoByGeneCodes(Experiment e, List<String> geneCodes) {
        return lowDimensionalDao.getLowDimensionalDtoByGeneCodes(e, geneCodes);
    }

    public LowDimensionalDto getLowDimensionalDtoByResolution(Experiment e, Resolution r) {
        return lowDimensionalDao.getLowDimensionalDtoByResolution2(e, r);
    }

    public List<HeatmapDto> getHeatmapDtos(Resolution r) {
        return heatmapDao.getHeatmapDtos(r);
    }

    public List<ViolinDto> getViolinDtos(Experiment e, Resolution r, List<String> geneCodes) {
        return violinDao.getViolinDtos(e, r, geneCodes);
    }

}
