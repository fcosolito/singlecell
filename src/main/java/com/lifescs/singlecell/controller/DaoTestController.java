package com.lifescs.singlecell.controller;

import org.springframework.web.bind.annotation.RestController;

import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.dto.model.TopMarkerDto;
import com.lifescs.singlecell.model.Resolution;

import lombok.AllArgsConstructor;

import java.util.List;

import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@AllArgsConstructor
public class DaoTestController {
    private ResolutionDao resolutionDao;

    @GetMapping("/test/resolution/top_markers")
    public List<TopMarkerDto> getMethodName() {
        Resolution r = resolutionDao.findResolutionById("exp1cluster_0.10").get();
        return resolutionDao.getTopMarkers(r, 5);
    }

}
