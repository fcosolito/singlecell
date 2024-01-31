package com.lifescs.singlecell.dao.file;

import java.io.InputStream;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import com.mongodb.client.gridfs.model.GridFSFile;

public abstract class FileDao {

    private GridFsOperations operations;

    public FileDao(GridFsOperations operations) {
        this.operations = operations;
    }

    GridFsResource findByFilename(String filename) {
        return operations.getResource(filename);
    }

    String save(InputStream s, String filename) {
        return operations.store(s, filename).toString();
    }

}
