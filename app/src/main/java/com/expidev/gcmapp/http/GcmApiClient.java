package com.expidev.gcmapp.http;

import me.thekey.android.TheKey;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class GcmApiClient
{
    private final String TAG = this.getClass().getSimpleName();
    
    private static final String BASE_URL_STAGE = "https://stage.sbr.global-registry.org/api";
    private static final String BASE_URL_PROD = "https://sbr.global-registry.org/api";
    private static final String MEASUREMENTS = "/measurements";
    private static final String TOKEN = "/token";
    
    public static void getToken(String ticket, TokenTask.TokenTaskHandler taskHandler)
    {
        new TokenTask(taskHandler).execute(BASE_URL_STAGE + MEASUREMENTS + TOKEN, ticket);
    }
    
    public static void getTicket(TheKey theKey, TicketTask.TicketTaskHandler taskHandler)
    {
        new TicketTask(taskHandler).execute(BASE_URL_STAGE + MEASUREMENTS + TOKEN, theKey);
    }
}
