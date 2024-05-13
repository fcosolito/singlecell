package com.lifescs.singlecell.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescs.singlecell.dao.plot.ViolinDao;
import com.lifescs.singlecell.dto.api.ViolinDto;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;
import com.lifescs.singlecell.model.ViolinGroup;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ViolinService {
  private ViolinDao violinDao;
  private ResolutionService resolutionService;

  public List<ViolinDto> getDtosByResolution(Experiment e, List<String> codes, Resolution r){
    List<ViolinDto> found =  violinDao.getDtosByResolution(e, codes, r);
    List<String> codesNotFound = new ArrayList<>();
    for (String code : codes){
      if (!found.stream().anyMatch(dto -> dto.getCode().equals(code))){
        codesNotFound.add(code);
      }
    }
    List<ViolinGroup> generated = resolutionService.addViolinGroupsForResolution(e, r, codesNotFound);
    resolutionService.saveViolinGroups(generated);
    List<ViolinDto> result = new ArrayList<>();
    result.addAll(found);
    result.addAll(violinDao.getDtosByResolution(e, codesNotFound, r));
    return result;
    
  }
}
