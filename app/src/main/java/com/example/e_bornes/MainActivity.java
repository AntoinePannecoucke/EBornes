package com.example.e_bornes;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.e_bornes.AsyncTasks.LoadBorne;
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
    private ImageButton list_btn, map_btn, settings_btn;
    private int currentFirstVisibleItem, currentVisibleItemCount, currentScrollState;
    private boolean isLoading;
    private BorneAdapter adapter;
    private int next_page = 1;
    private ListView listView;
    private ConstraintLayout settings;

    private double latitude, longitude;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        settings = findViewById(R.id.Parameters);

        listView = this.findViewById(R.id.list);

        bornes = new ListBornes();
        adapter = new BorneAdapter(MainActivity.this, bornes.getBornes());


        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        setOnClickListeners();

        loadBorneLaunch(this);

    }

    /**
     *
     */

    private void setOnClickListeners(){
        //Show listView
        list_btn = findViewById(R.id.list_btn);
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(ListView.VISIBLE);
                settings.setVisibility(ConstraintLayout.INVISIBLE);
            }
        });

        //Show Map
        map_btn = findViewById(R.id.map_btn);
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
                            return;
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(ListView.INVISIBLE);
                settings.setVisibility(ConstraintLayout.VISIBLE);
            }
        });
    }

    /**
     *
     * @param googleMap
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ClusterManager<Borne> clusterManager = new ClusterManager<Borne>(this, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        clusterManager.addItems(bornes.getBornes());

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 9.0f));
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
                isLoading = true;
                loadMoreData();
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

}