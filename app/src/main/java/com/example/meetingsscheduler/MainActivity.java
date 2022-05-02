package com.example.meetingsscheduler;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    FloatingActionButton addmeeting;

    TextView emptyview;

    private ArrayList<model_meeting> meetings_arraylist;
    private Database_Class db;
    private meeting_adapter meetingAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meetings_arraylist = new ArrayList<>();
        db = new Database_Class(MainActivity.this);

        notificationChannelCreate();
        runInBackground();

        recyclerView = findViewById(R.id.recyclerview);
        addmeeting = findViewById(R.id.add_meeting);
        emptyview = findViewById(R.id.empty_view);

        addmeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Add_meeting_details.class);
                startActivity(intent);
            }
        });

        // reading and showing database

        meetings_arraylist = db.view_meetings();
        Collections.sort(meetings_arraylist, new meetings_comparator());

        if (meetings_arraylist.size() > 0) {
            emptyview.setVisibility(View.INVISIBLE);
        } else {
            emptyview.setVisibility(View.VISIBLE);
        }

        meetingAdapter = new meeting_adapter(meetings_arraylist, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(meetingAdapter);
    }

    public void runInBackground() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                long last = 0;
                long gap = 60000;
                while (true) {
                    model_meeting first_meeting = null;
                    int idx = 0;
                    while (idx < meetings_arraylist.size()) {
                        if (meetings_arraylist.get(idx).IsOnline.equals("OFFLINE")) {
                            first_meeting = meetings_arraylist.get(idx);
                            break;
                        }
                        idx++;
                    }
                    if (first_meeting != null) {
                        double _latitude = Double.parseDouble(first_meeting.PLACE_LATITUDE);
                        double _longitude = Double.parseDouble(first_meeting.PLACE_LATITUDE);

                        long starttime = get_TIME_and_DATE_in_millis(first_meeting.Start_Time, first_meeting.Meeting_Date);
                        long currtime = System.currentTimeMillis();
                        long diff = currtime - last;
                        long val = diff / gap;
                        if (val >= 5) {
                            long diff_2 = currtime - starttime;
                            diff_2 = diff_2 / gap;
                            if (diff_2 <= 65) {
                                Location location = getlocation();
                                if(location!=null) {
                                    LatLng l1 = new LatLng(location.getLatitude(), location.getLongitude());
                                    LatLng l2 = new LatLng(_latitude, _longitude);
                                    long dist = (long) SphericalUtil.computeDistanceBetween(l1, l2);
                                    dist /= 1000;
                                    long needed_time = dist / 50;
                                    needed_time = needed_time * 60;
                                    send_notification();
                                }
                            }
                        }

                    }
                }
            }
        });
    }

    private void send_notification() {
        Intent intent1 = new Intent(MainActivity.this, notification_class.class);
        intent1.putExtra("message", "hurry up you are being late at your meeting!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long timeatclick = System.currentTimeMillis();
        long alarmtime = 1000 * 10;

        alarmManager.set(AlarmManager.RTC, timeatclick + alarmtime, pendingIntent);
    }

    public void notificationChannelCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String message = "meeting Added";
            String Channel_name = "meeting";

            NotificationChannel channel = new NotificationChannel(Channel_name, Channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(message);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    static class meetings_comparator implements Comparator<model_meeting> {
        public int compare(model_meeting m1, model_meeting m2) {
            String date1 = m1.Meeting_Date;
            String date2 = m2.Meeting_Date;
            String t1 = m1.Start_Time;
            String t2 = m2.Start_Time;

            long first = get_TIME_and_DATE_in_millis(t1, date1);
            long second = get_TIME_and_DATE_in_millis(t2, date2);

            Log.d("firsttimemilli", date1 + Long.toString(first));
            Log.d("secondtimemilli", date2 + Long.toString(second));
            if (first < second) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    static public long get_TIME_and_DATE_in_millis(String time1, String date1) {
        date1 += " ";
        time1 += ":00";
        date1 += time1;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d1 = null;
        try {
            d1 = sdf.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d1);
        return calendar.getTimeInMillis();
    }

    public Location getlocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final Location[] location1 = {null};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return location1[0];
        }
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                location1[0] = location;
            }
        });
        return location1[0];
    }
}