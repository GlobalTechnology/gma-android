package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.MinistriesDao;

import java.util.List;

/**
 * Created by William.Randall on 1/22/2015.
 */
public class AssociatedMinistriesService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private LocalBroadcastManager broadcastManager;

    public static final String ACTION_RETRIEVE_ASSOCIATED_MINISTRIES =
        AssociatedMinistriesService.class.getName() + ".ACTION_RETRIEVE_ASSOCIATED_MINISTRIES";

    public AssociatedMinistriesService()
    {
        super("AssociatedMinistriesService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        List<String> associatedMinistries = ministriesDao.retrieveAssociatedMinistries();
        Log.i(TAG, "Retrieved associated ministries");

        Intent broadcastToSettingsActivity = new Intent(ACTION_RETRIEVE_ASSOCIATED_MINISTRIES);
        broadcastToSettingsActivity.putExtra("associatedMinistries", listToCharSequenceArray(associatedMinistries));

        broadcastManager.sendBroadcast(broadcastToSettingsActivity);
    }

    private CharSequence[] listToCharSequenceArray(List<String> list)
    {
        return list.toArray(new CharSequence[list.size()]);
    }
}
