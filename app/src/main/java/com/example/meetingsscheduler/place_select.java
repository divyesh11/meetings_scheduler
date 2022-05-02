package com.example.meetingsscheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class place_select extends AppCompatActivity {

    private Database_Class database_class;

    private FusedLocationProviderClient client;
    private SupportMapFragment supportMapFragment;
    private int REQUEST_CODE = 111;

    private ConnectivityManager manager;
    private NetworkInfo networkInfo;
    private GoogleMap _Map;
    private Geocoder geocoder;
    private double selectedLat,selectedLng;
    List<Address> addresses;
    MarkerOptions markerOptions;

    Button OKbutton;

    String selected_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);

        OKbutton = findViewById(R.id.select_location);

        OKbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected_address!=null) {
                    entry_into_database();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please select a location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        markerOptions = new MarkerOptions();

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);

        client = LocationServices.getFusedLocationProviderClient(place_select.this);

        if (ActivityCompat.checkSelfPermission(place_select.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            getCurrentLocation();
        }
        else
        {
            ActivityCompat.requestPermissions(place_select.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
    }

    private void entry_into_database() {

        database_class = new Database_Class(getApplicationContext());

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String start_time = intent.getStringExtra("start_time");
        String mode = intent.getStringExtra("mode");
        String duration = intent.getStringExtra("duration");
        String with_whom = intent.getStringExtra("with_whom");
        String lat = String.valueOf(selectedLat);
        String lng = String.valueOf(selectedLng);
        String link = "";
        String place = selected_address;

        boolean complete = database_class.add_new_meeting(lat,lng,with_whom, duration, date, start_time, mode, place, link);

        // Go to main_activity

        if (complete)
        {
            Toast.makeText(getApplicationContext(), "Meeting added", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(place_select.this, MainActivity.class);
            startActivity(intent1);
        }
        else
        {
            Toast.makeText(place_select.this, "Some Error occurred!", Toast.LENGTH_SHORT).show();
            Toast.makeText(place_select.this, "Restart Application!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null)
                {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            _Map = googleMap;

                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));

                            Objects.requireNonNull(googleMap.addMarker(markerOptions)).showInfoWindow();

                            _Map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(@NonNull LatLng latLng) {

                                    googleMap.clear();

                                    int tries = 100;
                                    while(tries > 0 && networkInfo == null) {
                                        CheckConnection();
                                        tries -= 1;
                                    }

                                    if(networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable())
                                    {
                                        selectedLat = latLng.latitude;
                                        selectedLng = latLng.longitude;

                                        getAddress(selectedLat,selectedLng);
                                    }
                                    else
                                    {
                                        Toast.makeText(place_select.this, "Please Check Connection", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
                else
                {
                    Toast.makeText(place_select.this, "Please turn on GPS!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getCurrentLocation();
            }
            else
            {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void CheckConnection()
    {
        manager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();
    }

    private void getAddress(double mLat,double mLng)
    {
        geocoder = new Geocoder(place_select.this, Locale.getDefault());
        if(mLat!=0)
        {
            try
            {
                addresses = geocoder.getFromLocation(mLat,mLng,1);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if(addresses != null)
            {
                String address = addresses.get(0).getAddressLine(0);

                if(address != null)
                {
                    LatLng latLng = new LatLng(mLat,mLng);

                    selected_address = address;

                    markerOptions.position(latLng).title(address);

                    _Map.addMarker(markerOptions).showInfoWindow();
                }
                else
                {
                    Toast.makeText(place_select.this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(place_select.this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(place_select.this, "Latlng null", Toast.LENGTH_SHORT).show();
        }
    }
}