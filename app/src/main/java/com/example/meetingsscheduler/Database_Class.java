package com.example.meetingsscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Database_Class extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "meeting";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "meetings";

    // below variable is for our id column.
    private static final String ID_COL = "id";

    // below variable is for our second person name column
    private static final String NAME_COL = "with_whom";

    // below variable id for our meeting duration column.
    private static final String DURATION_COL = "duration_of_meeting";

    private static final String DATE_COL = "date_of_meeting";

    private static final String START_TIME_COL = "start_time";

    private static final String MODE_OF_MEETING = "mode_of_meeting";

    private static final String PLACE_OF_MEETING = "place_of_meeting";

    private static final String LINK_OF_MEETING = "link_of_meeting";

    private static final String LATITUDE_OF_PLACE = "latitude";

    private static final String LONGITUDE_OF_PLACE = "longitude";

    public Database_Class(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;
        query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + DURATION_COL + " TEXT,"
                + DATE_COL + " TEXT,"
                + START_TIME_COL + " TEXT,"
                + MODE_OF_MEETING + " TEXT,"
                + PLACE_OF_MEETING + " TEXT,"
                + LATITUDE_OF_PLACE + " TEXT,"
                + LONGITUDE_OF_PLACE + " TEXT,"
                + LINK_OF_MEETING + " TEXT) ";
        Log.d("query",query);

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean add_new_meeting(String lat,String lng,String with_whom,String duration,String date,String start_time,String mode,String place,String link)
    {
//        Log.d("lat",lat);
//        Log.d("lng",lng);
//        Log.d("place",place);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NAME_COL,with_whom);
        values.put(DURATION_COL,duration);
        values.put(DATE_COL,date);
        values.put(START_TIME_COL,start_time);
        values.put(MODE_OF_MEETING,mode);
        values.put(PLACE_OF_MEETING,place);
        values.put(LATITUDE_OF_PLACE,lat);
        values.put(LONGITUDE_OF_PLACE,lng);
        values.put(LINK_OF_MEETING,link);

        long val = db.insert(TABLE_NAME,null,values);

        db.close();

        return val != -1;
    }

    public ArrayList<model_meeting> view_meetings()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String read_query = "SELECT * FROM " + TABLE_NAME;

        Cursor viewmeetings = db.rawQuery(read_query,null);

        ArrayList<model_meeting> meetings = new ArrayList<>();

        if(viewmeetings.moveToFirst())
        {
            do{
                meetings.add(new model_meeting(viewmeetings.getString(1),
                        viewmeetings.getString(2),
                        viewmeetings.getString(3),
                        viewmeetings.getString(4),
                        viewmeetings.getString(5),
                        viewmeetings.getString(6),
                        viewmeetings.getString(7),
                        viewmeetings.getString(8),
                        viewmeetings.getString(9)));
            }while(viewmeetings.moveToNext());
        }

        viewmeetings.close();
        return meetings;
    }

    public void deleteMeeting(model_meeting meeting) {

        // on below line we are creating
        // a variable to write our database.
        SQLiteDatabase db = this.getWritableDatabase();

        String whereclause = "date_of_meeting = '"+meeting.getMeeting_Date()+"'" + " and " + "start_time = '"+meeting.getStart_Time()+"'";

        db.delete(TABLE_NAME, whereclause, null);
        db.close();
    }

}
