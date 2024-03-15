package com.lifescs.singlecell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lifescs.singlecell.dao.model.ResolutionDao;
import com.lifescs.singlecell.model.Experiment;
import com.lifescs.singlecell.model.Resolution;

@SpringBootTest
public class ResolutionDaoTest {
    private static Experiment experiment;

    @Autowired
    private ResolutionDao resolutionDao;

    @BeforeAll
    public static void beforeAll() {
        experiment = new Experiment("test");
        experiment.setId("exp1");
    }

    @Test
    public void experimentsAreEqual() {
        List<Resolution> resolutions = resolutionDao.findResolutionsByExperiment(experiment);
        assertEquals(resolutions.get(0).getExperiment(), resolutions.get(1).getExperiment());
    }

}
