package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.Cursor;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/26/2015.
 */
public class MinistriesDaoTest extends InstrumentationTestCase
{
    private final String TAG = getClass().getSimpleName();

    private MinistriesDao ministriesDao;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        Context context = new RenamingDelegatingContext(getInstrumentation().getTargetContext().getApplicationContext(), "test_");
        ministriesDao = MinistriesDao.getInstance(context);
    }

    private void cleanupDatabase()
    {
        Log.i(TAG, "Cleaning up database");
        ministriesDao.deleteAllData();
    }

    public void testSaveAssociatedMinistriesForTheFirstTime()
    {
        cleanupDatabase();
        ministriesDao.saveAssociatedMinistries(getTestAssignments());

        List<String> ministryNames = ministriesDao.retrieveAssociatedMinistries();
        assertNotNull(ministryNames);

        assertTrue(ministryNames.contains("Mock Ministry"));
        assertTrue(ministryNames.contains("Sub Ministry 1"));
        assertTrue(ministryNames.contains("Sub Ministry 2"));
        assertTrue(ministryNames.contains("Sub Ministry 3"));
        assertTrue(ministryNames.contains("Sub Ministry 4"));
        assertTrue(ministryNames.contains("Sub Ministry 5"));

        Cursor cursor = ministriesDao.retrieveAssignmentsCursor();
        int count = cursor.getCount();

        assertEquals(1, count);

        cursor.moveToFirst();
        assertEquals("A1", cursor.getString(cursor.getColumnIndex("id")));
        assertEquals("self-assigned", cursor.getString(cursor.getColumnIndex("team_role")));
    }

    public void testSaveAssociatedMinistriesWithUpdates()
    {
        testSaveAssociatedMinistriesForTheFirstTime();

        ArrayList<Assignment> assignments = getTestAssignments();
        Assignment testAssignment = assignments.get(0);
        testAssignment.setTeamRole("leader");

        ministriesDao.saveAssociatedMinistries(assignments);

        List<Ministry> ministryList = ministriesDao.retrieveAssociatedMinistriesList();
        assertEquals(6, ministryList.size());

        Cursor cursor = ministriesDao.retrieveAssignmentsCursor();
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        String teamRole = cursor.getString(cursor.getColumnIndex("team_role"));
        assertEquals("leader", teamRole);
    }

    private ArrayList<Assignment> getTestAssignments()
    {
        ArrayList<Assignment> assignments = new ArrayList<Assignment>();

        Assignment assignemnt1 = new Assignment();
        assignemnt1.setId("A1");
        assignemnt1.setMinistry(mockMinistry());
        assignemnt1.setTeamRole("self-assigned");

        assignments.add(assignemnt1);

        return assignments;
    }

    /**
     * Mock Ministry
     *   - Sub Ministry 1
     *     - Sub Ministry 2
     *       - Sub Ministry 3
     *     - Sub Ministry 4
     *       - Sub Ministry 5
     */
    private Ministry mockMinistry()
    {
        Ministry mockMinistry = new Ministry();
        Ministry subMinistry1 = new Ministry();
        Ministry subMinistry2 = new Ministry();
        Ministry subMinistry3 = new Ministry();
        Ministry subMinistry4 = new Ministry();
        Ministry subMinistry5 = new Ministry();

        mockMinistry.setName("Mock Ministry");
        subMinistry1.setName("Sub Ministry 1");
        subMinistry2.setName("Sub Ministry 2");
        subMinistry3.setName("Sub Ministry 3");
        subMinistry4.setName("Sub Ministry 4");
        subMinistry5.setName("Sub Ministry 5");

        mockMinistry.setMinistryId("M0");
        subMinistry1.setMinistryId("M1");
        subMinistry2.setMinistryId("M2");
        subMinistry3.setMinistryId("M3");
        subMinistry4.setMinistryId("M4");
        subMinistry5.setMinistryId("M5");

        mockMinistry.setMinistryCode("MOCK");
        subMinistry1.setMinistryCode("MIN_1");
        subMinistry2.setMinistryCode("MIN_2");
        subMinistry3.setMinistryCode("MIN_3");
        subMinistry4.setMinistryCode("MIN_4");
        subMinistry5.setMinistryCode("MIN_5");

        List<Ministry> subMinistryList1 = new ArrayList<Ministry>();
        List<Ministry> subMinistryList2 = new ArrayList<Ministry>();
        List<Ministry> subMinistryList3 = new ArrayList<Ministry>();
        List<Ministry> subMinistryList4 = new ArrayList<Ministry>();

        subMinistryList1.add(subMinistry1);
        subMinistryList2.add(subMinistry2);
        subMinistryList3.add(subMinistry3);
        subMinistryList2.add(subMinistry4);
        subMinistryList4.add(subMinistry5);

        subMinistry2.setSubMinistries(subMinistryList3);
        subMinistry4.setSubMinistries(subMinistryList4);
        subMinistry1.setSubMinistries(subMinistryList2);
        mockMinistry.setSubMinistries(subMinistryList1);

        return mockMinistry;
    }
}
