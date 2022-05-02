package com.example.meetingsscheduler;

import android.util.Log;

import java.sql.Time;
import java.util.Date;

public class model_meeting {

    String place;
    String Start_Time;
    String Meeting_Date;
    String duration;
    String link;
    String IsOnline;
    String with_whom;
    String PLACE_LATITUDE,PLACE_LONGITUDE;

    model_meeting()
    {
        place = "";
        Start_Time = "";
        Meeting_Date = "";
        duration = "";
        link = "";
        with_whom = "";
        IsOnline = "";
        PLACE_LATITUDE = "";
        PLACE_LONGITUDE = "";
    }

    model_meeting(String with_whom1,String duration1,String date1,String start_time1,String mode1,String place1,String place_lat,String place_lng,String link1)
    {
        place = place1;
        Start_Time = start_time1;
        Meeting_Date = date1;
        duration = duration1;
        with_whom = with_whom1;
        IsOnline = mode1;
        link = link1;
        PLACE_LATITUDE = place_lat;
        PLACE_LONGITUDE = place_lng;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getStart_Time() {
        return Start_Time;
    }

    public void setStart_Time(String start_Time) {
        Start_Time = start_Time;
    }

    public String getOnline() {
        return IsOnline;
    }

    public void setOnline(String online) {
        IsOnline = online;
    }

    public String getMeeting_Date() {
        return Meeting_Date;
    }

    public void setMeeting_Date(String meeting_Date) {
        Meeting_Date = meeting_Date;
    }

    public String getWith_whom() {
        return with_whom;
    }

    public void setWith_whom(String with_whom) {
        this.with_whom = with_whom;
    }

    public String getEndTime()
    {
        String start = getStart_Time();
        String D = getDuration();

        String[] parts1 = start.split(":");
        String[] parts2 = D.split(":");

        String H = parts1[0];
        String M = parts1[1];

        int hours1 = Integer.parseInt(H);
        int minutes1 = Integer.parseInt(M);

        String H1 = parts2[0];
        String M1 = parts2[1];

        int hours2 = Integer.parseInt(H1);
        int minutes2 = Integer.parseInt(M1);

        int hours = hours1 + hours2;
        int minutes = minutes1 + minutes2;

        if(minutes >= 60)
        {
            minutes %= 60;
            hours += 1;
        }
        hours %= 24;
        String ans = Integer.toString(hours);
        if(ans.length() < 2)
        {
            ans = '0' + ans;
        }
        ans += ":";
        if(minutes < 10)
        {
            ans += '0';
        }
        ans += Integer.toString(minutes);
        Log.d("ans",ans);
        return ans;
    }

}
