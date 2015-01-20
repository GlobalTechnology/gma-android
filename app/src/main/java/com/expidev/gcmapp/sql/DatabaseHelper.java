package com.expidev.gcmapp.sql;

import android.content.Context;

/**
 * Created by William.Randall on 1/19/2015.
 */
public class DatabaseHelper
{
    public static void retrieveMinistries(
        Context context,
        RetrieveMinistriesDatabaseTask.RetrieveMinistriesDatabaseTaskHandler taskHandler)
    {
        new RetrieveMinistriesDatabaseTask(taskHandler)
            .execute(context, TableNames.ASSOCIATED_MINISTRIES.getTableName());
    }

    public static void saveSessionToken(
        Context context,
        String sessionToken,
        SessionTokenDatabaseTask.SessionTokenDatabaseTaskHandler taskHandler)
    {
        new SessionTokenDatabaseTask((taskHandler)).execute(context, sessionToken);
    }
}
