package com.expidev.gcmapp;

import com.expidev.gcmapp.model.measurement.Measurement;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 2/9/2015.
 */
public class MeasurementsActivityUnitTest extends TestCase
{
    private Measurement buildTestMeasurement(String section, String iteration, String column)
    {
        Measurement testMeasurement = new Measurement();
        testMeasurement.setName(section + " " + iteration);
        testMeasurement.setTotal(Integer.parseInt(iteration) * 5);
        testMeasurement.setSection(section);
        testMeasurement.setColumn(column);
        testMeasurement.setMeasurementId(section.substring(0, 1) + iteration);
        testMeasurement.setPermLink("perm_link_" + section + "_" + iteration);

        return testMeasurement;
    }

    private List<Measurement> testMeasurements()
    {
        List<Measurement> measurementList = new ArrayList<>();

        Measurement win1 = buildTestMeasurement("win", "1", "_Training");
        Measurement win2 = buildTestMeasurement("win", "2", "_Training");
        Measurement win3 = buildTestMeasurement("win", "3", "_AnotherColumn");

        Measurement build1 = buildTestMeasurement("build", "1", "_Training");
        Measurement build2 = buildTestMeasurement("build", "2", "_Training");
        Measurement build3 = buildTestMeasurement("build", "3", "_AnotherColumn");
        Measurement build4 = buildTestMeasurement("build", "4", "_AnotherColumn");

        Measurement send1 = buildTestMeasurement("send", "1", "_Training");
        Measurement send2 = buildTestMeasurement("send", "2", "_Training");
        Measurement send3 = buildTestMeasurement("send", "3", "_AnotherColumn");

        measurementList.add(win1);
        measurementList.add(send1);
        measurementList.add(send3);
        measurementList.add(build2);
        measurementList.add(build1);
        measurementList.add(win2);
        measurementList.add(send2);
        measurementList.add(build4);
        measurementList.add(build3);
        measurementList.add(win3);

        return measurementList;
    }
}
