package com.lifescs.singlecell.dao.input;

import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.dto.csv.CellMetadataInputDto;
import com.lifescs.singlecell.mapper.PathMapper;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Project;

@Component
public class CellMetadataInputDao extends CsvDao<CellMetadataInputDto> {

    private PathMapper pathMapper;

    public CellMetadataInputDao(PathMapper pathMapper) {
        this.pathMapper = pathMapper;
    }

    public List<CellMetadataInputDto> readCSVToMetadataBeans(Project p, Experiment e) throws Exception {
        return readCSVToBeans(Path.of(pathMapper.metadataPath(p, e)),
                CellMetadataInputDto.class);
    }

}
