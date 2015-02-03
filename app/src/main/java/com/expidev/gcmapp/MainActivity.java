package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.GcmTheKey.GcmBroadcastReceiver;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.db.UserDao;
import com.expidev.gcmapp.map.MarkerRender;
import com.expidev.gcmapp.map.GcmMarker;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.model.User;
import com.expidev.gcmapp.service.AssociatedMinistriesService;
import com.expidev.gcmapp.service.AuthService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.service.Type;
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
import com.google.maps.android.clustering.ClusterManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private boolean targets;
    private boolean groups;
    private boolean churches;
    private boolean multiplyingChurches;
    private boolean trainingActivities;
    private boolean campuses;
    private SharedPreferences mapPreferences;
    private SharedPreferences preferences;
    private BroadcastReceiver broadcastReceiver;
    private String chosenMinistry;
    private List<Ministry> associatedMinistries;
    
    // try to cut down on api calls
    private boolean trainingDownloaded = false;
    private boolean ministriesDownloaded = false;
    
    private GoogleMap map;
    private ClusterManager<GcmMarker> clusterManager;
    
    private Ministry currentMinistry;
    private Assignment currentAssignment;
    
    private TextView mapOverlayText;
    
    private List<Training> allTraining;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        
        mapOverlayText = (TextView) findViewById(R.id.map_text);

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        getMapPreferences();
        
        setupBroadcastReceivers();

        theKey = TheKeyImpl.getInstance(getApplicationContext(), THEKEY_CLIENTID);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        gcmBroadcastReceiver = new GcmBroadcastReceiver(theKey, this);
        gcmBroadcastReceiver.registerReceiver(manager);
        
        // This call at times will create a null pointer. However, this is no big deal since it is call
        // again later. It could be removed here; however, this does help a returning user retrieve
        // their saved information a little quicker.
        getCurrentAssignment();

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
    
    private void trainingSearch(String ministryId, String mcc)
    {
        Log.i(TAG, "Training search");
        if (mcc == null) mcc = "slm";
        TrainingService.downloadTraining(this, ministryId, mcc);
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
        if (map != null) map.clear();
        getMapPreferences();
        getCurrentAssignment();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceivers();
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
        targets = preferences.getBoolean("targets", true);
        groups = preferences.getBoolean("groups", true);
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
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build();
            loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }
    
    private void getCurrentAssignment()
    {
        if (currentAssignment == null)
        {
            MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
            
            if (associatedMinistries == null)
            {
                chosenMinistry = preferences.getString("chosen_ministry", null);
                
                associatedMinistries = ministriesDao.retrieveAssociatedMinistriesList();
            }

            if (associatedMinistries == null || associatedMinistries.size() == 0)
            {
                Log.w(TAG, "No Associated Ministries");
                return;
            }

            if (chosenMinistry == null)
            {
                SharedPreferences.Editor editor = preferences.edit();
                
                int i = 0;
                boolean parent = false;
                do
                {
                    if (associatedMinistries.get(i).getParentId() == null) parent = true;
                }while (!parent);
                    
                currentMinistry = associatedMinistries.get(i);
                currentMinistry = associatedMinistries.get(1);
                
                chosenMinistry = currentMinistry.getName();
                editor.putString("chosen_ministry", chosenMinistry);

                currentAssignment = ministriesDao.retrieveCurrentAssignment(currentMinistry);
            }
            else
            {
                if (currentAssignment != null)
                {
                    for (Ministry ministry : associatedMinistries)
                    {
                        if (ministry.getName().equals(chosenMinistry))
                        {
                            currentMinistry = ministry;
                            currentAssignment = ministriesDao.retrieveCurrentAssignment(ministry);

                            if (currentAssignment != null)
                            {
                                Log.i(TAG, "current assignment: " + currentAssignment.toString());
                            }
                        }
                    }
                }
            }
        }

        if (map != null && currentAssignment != null) zoomToLocation();

        if (currentMinistry != null)
        {
            Log.i(TAG, "Current Ministry: " + currentMinistry.getName());
            String mcc = null;
            if (currentMinistry.hasSlm()) mcc = "slm";
            else if (currentMinistry.hasDs()) mcc = "ds";
            else if (currentMinistry.hasGcm()) mcc = "gcm";
            else if (currentMinistry.hasLlm()) mcc = "llm";
            
            String mccDisplay = "";
            if (mcc != null) mccDisplay = " (" + mcc +")";
            mapOverlayText.setText(currentMinistry.getName() + mccDisplay);

            if (!trainingDownloaded)
            {
                trainingSearch(currentMinistry.getMinistryId(), mcc);
            }
            
            addTrainingMakersToMap();
        }
    }
    
    private void setUpMap()
    {   
        if (trainingDownloaded) addTrainingMakersToMap();
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
        this.map = googleMap;
        
        setUpMap();
        
        clusterManager = new ClusterManager<>(this, map);
        clusterManager.setRenderer(new MarkerRender(this, map, clusterManager));
        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
    }

    private void zoomToLocation()
    {
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(currentAssignment.getLatitude(), currentAssignment.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(currentAssignment.getLocationZoom());

        map.moveCamera(center);
        map.moveCamera(zoom);
    }
    
    private void addTrainingMakersToMap()
    {
        // do not show training activities if turned off in map settings
        if (map != null && trainingActivities)
        {   
            for (Training training : allTraining)
            {
                GcmMarker marker = new GcmMarker(training.getName(), training.getLatitude(), training.getLongitude());
                clusterManager.addItem(marker);
            }
            clusterManager.cluster();
        }
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

                    Type type = (Type) intent.getSerializableExtra(BroadcastUtils.ACTION_TYPE);
                    
                    switch (type)
                    {
                        case AUTH:
                            UserDao userDao = UserDao.getInstance(context);
                            User user = userDao.retrieveUser();
                            actionBar.setTitle("Welcome " + user.getFirstName());

                            String sessionTicket = preferences.getString("session_ticket", null);
                            Log.i(TAG, "Session Ticket: " + sessionTicket);

                            if (!ministriesDownloaded)
                            {
                                AssociatedMinistriesService.retrieveAllMinistries(getApplicationContext(), sessionTicket);
                            }
                            
                            break;
                        case TRAINING:
                            Log.i(TAG, "Training search complete and training saved");
                            
                            trainingDownloaded = true;
                            
                            TrainingDao trainingDao = TrainingDao.getInstance(context);
                            allTraining = trainingDao.getAllMinistryTraining(currentMinistry.getMinistryId());
                            
                            addTrainingMakersToMap();
                            
                            break;
                        case RETRIEVE_ALL_MINISTRIES:
                            Serializable data = intent.getSerializableExtra("allMinistries");

                            if(data != null)
                            {
                                List<Ministry> allMinistries = (ArrayList<Ministry>) data;
                                AssociatedMinistriesService.saveAllMinistries(getApplicationContext(), allMinistries);
                                ministriesDownloaded = true;
                            }
                            else
                            {
                                Log.e(TAG, "Failed to retrieve ministries");
                                finish();
                            }
                            break;
                        case SAVE_ALL_MINISTRIES:
                            Log.i(TAG, "All ministries saved to local storage");
                            break;
                        case SAVE_ASSOCIATED_MINISTRIES:
                            getCurrentAssignment();
                            break;
                        default:
                            Log.i(TAG, "Unhandled Type: " + type);
                    }
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
