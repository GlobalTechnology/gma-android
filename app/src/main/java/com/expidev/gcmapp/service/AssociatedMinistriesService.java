package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.MinistriesDao;

import java.util.List;

import static com.expidev.gcmapp.service.Action.RETRIEVE_ASSOCIATED_MINISTRIES;

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

    /////////////////////////////////////////////////////
    //           Lifecycle Handlers                   //
    ////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        final Action action = (Action)intent.getSerializableExtra("action");

        switch(action)
        {
            case RETRIEVE_ASSOCIATED_MINISTRIES:
                retrieveMinistries();
                break;
            default:
                break;
        }
    }


    /////////////////////////////////////////////////////
    //           Service API                          //
    ////////////////////////////////////////////////////
    private static Intent baseIntent(final Context context, Bundle extras)
    {
        final Intent intent = new Intent(context, AssociatedMinistriesService.class);

        if(extras != null)
        {
            intent.putExtras(extras);
        }

        return intent;
    }

    public static void retrieveMinistries(final Context context)
    {
        Bundle extras = new Bundle(1);
        extras.putSerializable("action", RETRIEVE_ASSOCIATED_MINISTRIES);

        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void retrieveMinistries()
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
