package com.example.meetingsscheduler;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Add_meeting_details extends AppCompatActivity {

    private notification_class notification_object;

    TimePickerDialog picker;
    final Calendar myCalendar= Calendar.getInstance();

    EditText With_whom;
    EditText Date;
    EditText Time;
    EditText Duration;
    EditText Link;

    Button add_meeting;

    RadioGroup radioGroup;

    boolean IsOnline = true;

    private Database_Class database_class;

    int Hour = 0;

    Boolean approved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting_details);

        With_whom = findViewById(R.id.With_whom);
        Date = findViewById(R.id.Date);
        Time = findViewById(R.id.Time);
        Duration = findViewById(R.id.Duration);
        Link = findViewById(R.id.Link);

        radioGroup = findViewById(R.id.radiogroup);

        Time.setInputType(InputType.TYPE_NULL);
        Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);

                picker = new TimePickerDialog(Add_meeting_details.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int H, int M) {
                        Hour = H;
                        String minute = "";
                        if(M < 10)
                        {
                            minute += "0";
                        }
                        minute += Integer.toString(M);
                        Time.setText(H + ":" + minute);
                    }
                },hour,minutes,true);
                picker.show();
            }
        });

        Duration.setInputType(InputType.TYPE_NULL);
        Duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);

                picker = new TimePickerDialog(Add_meeting_details.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int H, int M) {
                        Hour = H;
                        H = H % 12;
                        String minute = "";
                        if(M < 10)
                        {
                            minute += "0";
                        }
                        minute += Integer.toString(M);
                        Duration.setText(H + ":" + minute);
                    }
                },hour,minutes,false);
                picker.show();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = group.getCheckedRadioButtonId();

                View radioButton = group.findViewById(radioButtonID);

                int idx = group.indexOfChild(radioButton);

                if(idx == 0) // if meeting mode is online
                {
                    IsOnline = true;
                }
                else { // if meeting mode is offline
                    IsOnline = false;
                }
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                UpdateDateEdittext();
            }
        };

        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Add_meeting_details.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        if(!IsOnline)
        {
            Link.setHint("Don't need to Enter link");
        }
        else
        {
            Link.setHint("Please Enter Link Of Meeting");
        }

        database_class = new Database_Class(getApplicationContext());

        // when user click on add_meeting button
        add_meeting = findViewById(R.id.add_meeting);
        add_meeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String withwhom = With_whom.getText().toString();
                String mode = "ONLINE";
                String date = Date.getText().toString();
                String start = Time.getText().toString();
                start = SetUpTimeInFormat(start);
                String duration = Duration.getText().toString();
                duration = SetUpTimeInFormat(duration);
                String place = "";
                String link = Link.getText().toString();

                if(get_TIME_and_DATE_in_millis(start,date)) {
                    if (IsOnline) {
                        if (link.length() > 0) {
                            boolean complete = database_class.add_new_meeting("", "", withwhom, duration, date, start, mode, place, link);

                            // Go to main_activity

                            if (complete) {
                                Toast.makeText(getApplicationContext(), "Meeting added", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Add_meeting_details.this, MainActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(Add_meeting_details.this, "Please Enter Link of Meeting", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mode = "OFFLINE";
                        Intent intent = new Intent(Add_meeting_details.this, place_select.class);
                        intent.putExtra("with_whom", withwhom);
                        intent.putExtra("mode", mode);
                        intent.putExtra("date", date);
                        intent.putExtra("start_time", start);
                        intent.putExtra("duration", duration);
                        startActivity(intent);
                    }
                }
                else
                {
                    Toast.makeText(Add_meeting_details.this, "Please Enter valid time and date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String SetUpTimeInFormat(String time)
    {
        String[] parts1 = time.split(":");
        String first = parts1[0];
        String second = parts1[1];
        if(first.length() < 2)
        {
            first = "0" + first;
        }
        if(second.length() < 2)
        {
            second = "0" + second;
        }
        String ans = first + ":" + second;
        return ans;
    }

    private void UpdateDateEdittext(){
        String myFormat="dd/MM/yyyy";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);
        Date.setText(dateFormat.format(myCalendar.getTime()));
    }

    static public boolean get_TIME_and_DATE_in_millis(String time1, String date1) {
        date1 += " ";
        time1 += ":00";
        date1 += time1;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        java.util.Date d1 = null;
        try {
            d1 = sdf.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d1);

        long first = calendar.getTimeInMillis();
        long second = System.currentTimeMillis();
        return first > second;
    }
}