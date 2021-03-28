package com.example.e_bornes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.example.e_bornes.Model.Borne;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowBorne extends AppCompatActivity  implements OnMapReadyCallback {

    private Borne borne;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_borne);
        borne = (Borne) getIntent().getSerializableExtra("borne");
        showData();
        checkLocationEnable();
    }

    private void showData(){
        TextView name = this.findViewById(R.id.borne_name);
        name.setText(borne.getName());

        TextView address = this.findViewById(R.id.borne_address);
        address.setText(borne.getAddress());

        TextView accessibility = this.findViewById(R.id.borne_accessibility);
        accessibility.setText(borne.getAccessibility());

        TextView access = this.findViewById(R.id.borne_access);
        access.setText(borne.getAccess());

        TextView power = this.findViewById(R.id.borne_max_power);
        power.setText(getText(R.string.power).toString() + " : " + borne.getPower() +"V");

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_borne);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
            .title(borne.getTitle())
            .position(borne.getPosition())
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(borne.getPosition(), 12.0f));
    }

    private void checkLocationEnable(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        else {
            mapFragment.getMapAsync(ShowBorne.this);
        }
    }
}