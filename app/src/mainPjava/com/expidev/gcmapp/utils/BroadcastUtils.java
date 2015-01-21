package com.expidev.gcmapp.utils;

import android.content.Intent;
import android.content.IntentFilter;

import com.expidev.gcmapp.service.AuthService;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public final class BroadcastUtils
{
    public static final String ACTION_START = AuthService.class.getName() + ".ACTION_START";
    public static final String ACTION_RUNNING = AuthService.class.getName() + ".ACTION_RUNNING";
    public static final String ACTION_STOP = AuthService.class.getName() + ".ACTION_STOP";

    public static final String TICKET_RECEIVED = AuthService.class.getName() + ".TICKET_RECEIVED";

    public static Intent startBroadcast()
    {
        return new Intent(ACTION_START);
    }

    public static Intent runningBroadcast()
    {
        return new Intent(ACTION_RUNNING);
    }

    public static Intent stopBroadcast()
    {
        return new Intent(ACTION_STOP);
    }

    public static IntentFilter startFilter()
    {
        return new IntentFilter(ACTION_START);
    }

    public static IntentFilter runningFilter()
    {
        return new IntentFilter(ACTION_RUNNING);
    }

    public static IntentFilter stopFilter()
    {
        return new IntentFilter(ACTION_STOP);
    }

    public static Intent ticketReceivedBroadcast(String ticket)
    {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra("ticket", ticket);
        return intent;
    }
}
