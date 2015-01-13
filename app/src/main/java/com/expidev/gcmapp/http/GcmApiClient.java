package com.expidev.gcmapp.http;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class GcmApiClient
{
    private final String TAG = this.getClass().getSimpleName();
    
    private static final String BASE_URL_STAGE = "https://stage.sbr.global-registry.org/api";
    private static final String BASE_URL_PROD = "https://sbr.global-registry.org/api";
    private static final String MEASUREMENTS = "/measurements";
    
    public static void getToken(String guid, TokenTask.TokenTaskHandler taskHandler)
    {
        String url = BASE_URL_STAGE + MEASUREMENTS;
        new TokenTask(taskHandler).execute(url, guid);
    }
}
