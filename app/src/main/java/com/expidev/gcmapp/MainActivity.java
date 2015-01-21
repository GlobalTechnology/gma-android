package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.expidev.gcmapp.GPSService.GPSTracker;
import com.expidev.gcmapp.GcmTheKey.GcmBroadcastReceiver;
import com.expidev.gcmapp.GcmTheKey.GcmTheKeyHelper;
import com.expidev.gcmapp.http.GcmApiClient;
import com.expidev.gcmapp.http.TicketTask;
import com.expidev.gcmapp.http.TokenTask;
import com.expidev.gcmapp.model.User;
import com.expidev.gcmapp.sql.DatabaseHelper;
import com.expidev.gcmapp.sql.SessionTokenDatabaseTask;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;
import com.expidev.gcmapp.utils.Device;
import com.expidev.gcmapp.utils.GcmProperties;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback
{
    private final String TAG = this.getClass().getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    private Properties properties;
    private TheKey theKey;
    private long keyClientId;
    private LocalBroadcastManager manager;
    private GcmBroadcastReceiver gcmBroadcastReceiver;
    private ActionBar actionBar;
    private GPSTracker gps;
    private boolean targets;
    private boolean groups;
    private boolean churches;
    private boolean multiplyingChurches;
    private boolean trainingActivities;
    private boolean campuses;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();

        getMapPreferences();
        
        populateDummyMinistries();

        getProperties();

        keyClientId = Long.parseLong(properties.getProperty("TheKeyClientId", ""));

        theKey = TheKeyImpl.getInstance(getApplicationContext(), keyClientId);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        gcmBroadcastReceiver = new GcmBroadcastReceiver(theKey, this);
        gcmBroadcastReceiver.registerReceiver(manager);

        if (Device.isConnected(getApplicationContext()))
        {
           handleLogin();
        }
        else
        {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
        if (checkPlayServices())
        {
            SupportMapFragment map = (SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map);
            map.getMapAsync(this);
        }
    }

    private void handleLogin()
    {
        // check for previous login sessions
        if (theKey.getGuid() == null)
        {
            login();
        }
        else
        {
            GcmApiClient.getTicket(theKey, new TicketTask.TicketTaskHandler()
            {
                @Override
                public void taskComplete(String ticket)
                {
                    GcmApiClient.getToken(ticket, new TokenTask.TokenTaskHandler()
                    {
                        @Override

                        public void taskComplete(JSONObject object)
                        {
                            Log.i(TAG, "Task Complete");
                            User user = GcmTheKeyHelper.createUser(object);
                            String welcomeMessage = "Welcome " + user.getFirstName();
                            actionBar.setTitle(welcomeMessage);

                            writeSessionTokenToDatabase(getTokenFromJson(object));
                        }

                        @Override
                        public void taskFailed(String status)
                        {
                            Log.i(TAG, "Task Failed. Status: " + status);
                        }
                    });
                }

                @Override
                public void taskFailed()
                {

                }
            });
        }
    }

    private String getTokenFromJson(JSONObject json)
    {
        try
        {
            return json.getString("session_ticket");
        }
        catch(JSONException e)
        {
            Log.e(TAG, "Failed to get session token from json: " + e.getMessage());
            return null;
        }
    }

    private void writeSessionTokenToDatabase(String sessionToken)
    {
        DatabaseHelper.saveSessionToken(this, sessionToken, new SessionTokenDatabaseTask.SessionTokenDatabaseTaskHandler()
        {
            @Override
            public void taskComplete()
            {
                Log.i(TAG, "Successfully saved session token to database");
            }

            @Override
            public void taskFailed(String reason)
            {
                Log.e(TAG, "Failed to save session token: " + reason);
            }
        });
    }

    //TODO: Remove this when join ministry works
    private void populateDummyMinistries()
    {
        DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(this);

        SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();
        String tableName = TableNames.ASSOCIATED_MINISTRIES.getTableName();
        
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);

        // Only add the dummy rows if none exist
        if(cursor.getCount() == 0)
        {
            database.beginTransaction();

            ContentValues guatemala = new ContentValues();
            guatemala.put("ministry_id", "1");
            guatemala.put("name", "Guatemala");
            guatemala.put("team_role", "self-assigned");
            guatemala.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert(tableName, null, guatemala);

            ContentValues bridgesUcf = new ContentValues();
            bridgesUcf.put("ministry_id", "2");
            bridgesUcf.put("name", "Bridges UCF");
            bridgesUcf.put("team_role", "member");
            bridgesUcf.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert(tableName, null, bridgesUcf);

            ContentValues antioch21 = new ContentValues();
            antioch21.put("ministry_id", "3");
            antioch21.put("name", "Antioch21 Church");
            antioch21.put("team_role", "leader");
            antioch21.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert(tableName, null, antioch21);

            ContentValues random = new ContentValues();
            random.put("ministry_id", "4");
            random.put("name", "Random");
            random.put("team_role", "inherited_leader");
            random.put("last_synced", "datetime(2015-01-15 11:30:00)");
            database.insert(tableName, null, random);

            database.setTransactionSuccessful();
            database.endTransaction();
        }
        cursor.close();
        database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent goToSettings = new Intent(this, SettingsActivity.class);
            startActivity(goToSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        getMapPreferences();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        manager.unregisterReceiver(gcmBroadcastReceiver);
        gps.stopUsingGPS();
    }

    public void joinNewMinistry(MenuItem menuItem)
    {
        //TODO: implement join new ministry
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Join new ministry")
                .setMessage("Choose a new ministry to join:")
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void reset(MenuItem menuItem)
    {
        //TODO: implement reset: clear local data-model, download from server
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Reset")
                .setMessage("Re-downloading information...")
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void logout(MenuItem menuItem)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        theKey.logout();
                        dialog.dismiss();
                        login();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();  
    }

    private void getProperties()
    {
        GcmProperties gcmProperties = new GcmProperties(this);
        properties = gcmProperties.getProperties("gcm_properties.properties");
    }
    
    private void getMapPreferences()
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        targets = preferences.getBoolean("targets", true);
        groups = preferences.getBoolean("preferences", true);
        churches = preferences.getBoolean("churches", true);
        multiplyingChurches = preferences.getBoolean("multiplyingChurches", true);
        trainingActivities = preferences.getBoolean("trainingActivities", true);
        campuses = preferences.getBoolean("campuses", true);
    }
    
    private void login()
    {   
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null)
        {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().clientId(keyClientId).build();
            loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
    
    public void mapOptions(View view)
    {
        Log.i(TAG, "Map options");
        Intent intent = new Intent(this, MapSettings.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.i(TAG, "On Map Ready");
        
        gps = new GPSTracker(this);
        
        if (gps.canGetLocation())
        {
            LatLng latLng = new LatLng(gps.getLatitude(), gps.getLongitude());
            zoomToLocation(latLng, googleMap);
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("You"));
        }
        else
        {
            gps.showSettingsAlert();
        }
    }
    
    private void zoomToLocation(LatLng latLng, GoogleMap map)
    {
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(center);
        map.moveCamera(zoom);
    }
}
