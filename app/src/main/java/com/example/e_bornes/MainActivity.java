package com.example.e_bornes;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.BoolRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_bornes.AsyncTasks.LoadBorne;
import com.example.e_bornes.AsyncTasks.LoadBorneFilter;
import com.example.e_bornes.Model.Borne;
import com.example.e_bornes.Model.ListBornes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AbsListView.OnScrollListener {

    private SupportMapFragment mapFragment;
    private ListBornes bornes;
    private int currentFirstVisibleItem, currentVisibleItemCount, currentScrollState;
    private boolean isLoading;
    private BorneAdapter adapter;
    private int next_page = 1;
    private ListView listView;
    private ConstraintLayout settings;
    private EditText zip_input;
    private SharedPreferences sharedPref;
    private @SuppressLint("UseSwitchCompatOrMaterialCode") Switch zoom;

    private double latitude, longitude;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        settings = findViewById(R.id.Parameters);

        listView = this.findViewById(R.id.list);

        zip_input = this.findViewById(R.id.zip_input);
        zoom = this.findViewById(R.id.zoom_switch);

        bornes = new ListBornes();
        adapter = new BorneAdapter(MainActivity.this, bornes.getBornes());

        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        setOnClickListeners();

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.contains(getString(R.string.filter))) {
            zip_input.setText(sharedPref.getString(getString(R.string.filter), ""));
            loadBorneFilterLaunch(this, sharedPref.getString(getString(R.string.filter), ""));
            zoom.setChecked(sharedPref.getBoolean(getString(R.string.zoom), false));
        } else {
            loadBorneLaunch(this);
        }

    }

    /**
     *
     */

    private void setOnClickListeners(){
        //Show listView
        ImageButton list_btn = findViewById(R.id.list_btn);
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(ListView.VISIBLE);
                settings.setVisibility(ConstraintLayout.INVISIBLE);
            }
        });

        //Show Map
        ImageButton map_btn = findViewById(R.id.map_btn);
        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setVisibility(ConstraintLayout.INVISIBLE);
                listView.setVisibility(ListView.INVISIBLE);
                checkLocationEnable();
                Log.d("number", "" + bornes.getNumberMaxBornes());
                if (bornes.getBornes().size() < bornes.getNumberMaxBornes()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setMessage(R.string.noLoadMessage);
                    alertDialogBuilder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        ImageButton settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(ListView.INVISIBLE);
                settings.setVisibility(ConstraintLayout.VISIBLE);
            }
        });

        Button save_settings_btn = findViewById(R.id.save_settings_btn);
        save_settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filter = zip_input.getText().toString();
                if (filter.length() == 2 || filter.length() == 5) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.filter), filter);
                    editor.apply();
                    loadBorneFilterLaunch(MainActivity.this, filter);
                }
            }
        });

        Button remove_filter_btn = findViewById(R.id.remove_filter_btn);
        remove_filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zip_input.setText("");
                sharedPref.edit().remove(getString(R.string.filter)).apply();
                bornes.clear();
                loadBorneLaunch(MainActivity.this);
            }
        });

        zoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("switch", "onCheckedChanged: ");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.zoom), compoundButton.isChecked());
                editor.apply();
            }
        });
    }

    /**
     *
     * @param googleMap
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        ClusterManager<Borne> clusterManager = new ClusterManager<Borne>(this, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        //googleMap.setOnMarkerClickListener(clusterManager);

        clusterManager.addItems(bornes.getBornes());
        if (sharedPref.getBoolean(getString(R.string.zoom), false)) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 9.0f));
        }
    }

    /**
     *
     * @param hasCapture
     */

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     *
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
    }

    /**
     *
     * @param view
     * @param scrollState
     */

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        this.isScrollCompleted();
    }

    /**
     *
     */

    private void isScrollCompleted() {
        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE &&  this.currentFirstVisibleItem == bornes.getBornes().size() - this.currentVisibleItemCount) {
            if(!isLoading){
                if (bornes.getNumberMaxBornes() > bornes.getBornes().size()) {
                    isLoading = true;
                    loadMoreData();
                }
            }
        }
    }

    /**
     *
     */

    private void loadMoreData(){
        LoadBorne task = new LoadBorne();
        task.execute(bornes, adapter, next_page*50);
        next_page++;
        isLoading = false;
    }

    /**
     *
     */

    private void checkLocationEnable(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        else {
            listView.setVisibility(ListView.INVISIBLE);
            mapFragment.getMapAsync(MainActivity.this);
            GPSTracker tracker = new GPSTracker(this);
            if (!tracker.canGetLocation()) {
                tracker.showSettingsAlert();
            } else {
                latitude = tracker.getLatitude();
                longitude = tracker.getLongitude();
            }
        }
    }

    /**
     *
     * @param context
     * @return
     */

    private boolean checkInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
        }
        return false;
    }

    /**
     *
     * @param context
     */

    private void loadBorneLaunch(Context context){
        if (checkInternet(context)){
            LoadBorne task = new LoadBorne();
            task.execute(bornes, adapter, 0);
        }
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setMessage(R.string.InternetCheckMessage);
            alertDialogBuilder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private void loadBorneFilterLaunch(Context context, String filter){
        if (checkInternet(context)){
            LoadBorneFilter task = new LoadBorneFilter();
            bornes.clear();
            task.execute(bornes, adapter, 0, filter);
        }
        else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setMessage(R.string.InternetCheckMessage);
            alertDialogBuilder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}