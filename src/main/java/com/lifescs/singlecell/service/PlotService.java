package com.lifescs.singlecell.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.model.PlotDao;
import com.lifescs.singlecell.dto.api.HeatmapDto;
import com.lifescs.singlecell.dto.api.LowDimentionalDtoByGene;
import com.lifescs.singlecell.dto.api.LowDimentionalDtoByResolution;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PlotService {
    private PlotDao plotDao;

    public LowDimentionalDtoByGene getLowDimentionalDtoByGeneCodes(Experiment e, List<String> geneCodes) {
        return plotDao.getLowDimentionalDtoByGeneCodes(e, geneCodes);
    }

    public LowDimentionalDtoByResolution getLowDimentionalDtoByResolution(Experiment e, Resolution r) {
        return plotDao.getLowDimentionalDtoByResolution2(e, r);
    }

    public List<HeatmapDto> getHeatmapDtos(Resolution r) {
        return plotDao.getHeatmapDtos(r);
    }

    public List<ViolinDto> getViolinDtos(Experiment e, Resolution r, List<String> geneCodes) {
        return plotDao.getViolinDtos(e, r, geneCodes);
    }

}
