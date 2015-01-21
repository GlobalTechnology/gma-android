package com.expidev.gcmapp.http;

import android.content.Context;

import com.expidev.gcmapp.service.AuthService;

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
    
    public static void getTicket(Context context)
    {
        AuthService.getTicket(context, BASE_URL_STAGE + MEASUREMENTS + TOKEN);
    }
}
