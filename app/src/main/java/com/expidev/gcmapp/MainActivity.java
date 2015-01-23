package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.expidev.gcmapp.db.UserDao;
import com.expidev.gcmapp.model.User;
import com.expidev.gcmapp.service.AuthService;
import com.expidev.gcmapp.utils.BroadcastUtils;
import com.expidev.gcmapp.utils.Device;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;


public class MainActivity extends ActionBarActivity
    implements OnMapReadyCallback
{
    private final String TAG = this.getClass().getSimpleName();

    private final String PREF_NAME = "gcm_prefs";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private TheKey theKey;
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
    private SharedPreferences mapPreferences;
    private SharedPreferences preferences;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();

        getMapPreferences();
        
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        setupBroadcastReceivers();

        theKey = TheKeyImpl.getInstance(getApplicationContext(), THEKEY_CLIENTID);

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
            AuthService.authorizeUser(this);
        }
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
        removeBroadcastReceivers();
        gps.stopUsingGPS();
    }

    public void joinNewMinistry(MenuItem menuItem)
    {
        final Context context = this;
        if (Device.isConnected(getApplicationContext()))
        {
            if (theKey.getGuid() == null)
            {
                login();
            }
            else
            {
                Intent goToJoinMinistryPage = new Intent(context, JoinMinistryActivity.class);
                startActivity(goToJoinMinistryPage);
            }
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Internet Necessary")
                .setMessage("You need Internet access to access this page")
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

            alertDialog.show();
        }
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
    
    private void getMapPreferences()
    {
        mapPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        targets = mapPreferences.getBoolean("targets", true);
        groups = mapPreferences.getBoolean("groups", true);
        churches = mapPreferences.getBoolean("churches", true);
        multiplyingChurches = mapPreferences.getBoolean("multiplyingChurches", true);
        trainingActivities = mapPreferences.getBoolean("trainingActivities", true);
        campuses = mapPreferences.getBoolean("campuses", true);
    }
    
    private void login()
    {   
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null)
        {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build();
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

    private void setupBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);

        this.broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (BroadcastUtils.ACTION_START.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Started");
                }
                else if (BroadcastUtils.ACTION_RUNNING.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Running");
                }
                else if (BroadcastUtils.ACTION_STOP.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Done");

                    UserDao userDao = UserDao.getInstance(context);
                    User user = userDao.retrieveUser();
                    actionBar.setTitle("Welcome " + user.getFirstName());
                    
                    String sessionTicket = preferences.getString("session_ticket", null);
                    Log.i(TAG, "Session Ticket: " + sessionTicket);
                }
            }
        };

        manager.registerReceiver(broadcastReceiver, BroadcastUtils.startFilter());
        manager.registerReceiver(broadcastReceiver, BroadcastUtils.runningFilter());
        manager.registerReceiver(broadcastReceiver, BroadcastUtils.stopFilter());
    }

    private void removeBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(broadcastReceiver);
        manager.unregisterReceiver(gcmBroadcastReceiver);
        broadcastReceiver = null;
        gcmBroadcastReceiver = null;
    }
}
