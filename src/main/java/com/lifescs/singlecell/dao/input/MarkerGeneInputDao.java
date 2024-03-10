package com.lifescs.singlecell.dao.input;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.csv.MarkerGeneInputDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MarkerGeneInputDao extends CsvDao {
    private PathMapper pathMapper;

    public List<MarkerGeneInputDto> readCSVToMetadataBeans(Project p, Experiment e) throws Exception {
        return readCSVToBeans(Path.of(pathMapper.markersPath(p, e)), MarkerGeneInputDto.class);
    }

}
