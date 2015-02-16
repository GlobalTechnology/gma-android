package com.expidev.gcmapp;

import com.expidev.gcmapp.model.Measurement;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 2/9/2015.
 */
public class MeasurementsActivityUnitTest extends TestCase
{
    /**
     * Should sort the list into this structure:
     *   - _AnotherColumn
     *     Win 3
     *     Build 3
     *     Build 4
     *     Send 3
     *   - _Training
     *     Win 1
     *     Win 2
     *     Build 1
     *     Build 2
     *     Send 1
     *     Send 2
     */
    public void testSortMeasurements()
    {
        MeasurementsActivity measurementsActivity = new MeasurementsActivity();
        List<Measurement> sortedList = measurementsActivity.sortMeasurements(testMeasurements());

        assertEquals("w3", sortedList.get(0).getMeasurementId());
        assertEquals("b3", sortedList.get(1).getMeasurementId());
        assertEquals("b4", sortedList.get(2).getMeasurementId());
        assertEquals("s3", sortedList.get(3).getMeasurementId());
        assertEquals("w1", sortedList.get(4).getMeasurementId());
        assertEquals("w2", sortedList.get(5).getMeasurementId());
        assertEquals("b1", sortedList.get(6).getMeasurementId());
        assertEquals("b2", sortedList.get(7).getMeasurementId());
        assertEquals("s1", sortedList.get(8).getMeasurementId());
        assertEquals("s2", sortedList.get(9).getMeasurementId());
    }

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
