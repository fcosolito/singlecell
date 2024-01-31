package com.lifescs.singlecell.dao.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.GeneExpressionMap;

@Component
public class GeneExpressionMapDao { // extends FileDao
    @Value("${test.mapFile}")
    private String mapPathString;
    @Value("${test.inputDirectory}")
    private String projectDirectory;

    public GeneExpressionMap loadMap(Experiment ex) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(pathFromExperiment(ex) + mapPathString))) {
            return (GeneExpressionMap) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveMap(GeneExpressionMap map, Experiment ex) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(pathFromExperiment(ex) + mapPathString))) {
            oos.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String pathFromExperiment(Experiment e) {
        return projectDirectory + e.getId() + "/";
    }
}
