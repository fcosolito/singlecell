package com.lifescs.singlecell.dao.file;

import java.io.InputStream;

import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
// Class to manage experiment files inside a mongo grid file system
public abstract class FileDao {

    private GridFsOperations operations;

    GridFsResource findByFilename(String filename) {
        return operations.getResource(filename);
    }

    String save(InputStream s, String filename) {
        return operations.store(s, filename).toString();
    }

}
