package com.example.meetingsscheduler;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class meeting_adapter extends RecyclerView.Adapter<meeting_adapter.ViewHolder>{

    private ArrayList<model_meeting> meetings;
    private Context context;

    public meeting_adapter(ArrayList<model_meeting> meetingarray,Context passcontext)
    {
        this.meetings = meetingarray;
        this.context = passcontext;
    }

    @NonNull
    @Override
    public meeting_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull meeting_adapter.ViewHolder holder, int position) {
        model_meeting this_meeting = meetings.get(position);
        holder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database_Class db = new Database_Class(context);

                db.deleteMeeting(this_meeting);

                Intent intent = new Intent(context,MainActivity.class);
                context.startActivity(intent);
            }
        });
        holder.withwhom.setText("With : " + this_meeting.with_whom);
        holder.endtime.setText(this_meeting.getEndTime());
        holder.start_time.setText(this_meeting.Start_Time);
        holder.duration.setText(this_meeting.duration);
        holder.place.setText( "Place : " + this_meeting.place);
        holder.dateofmeeting.setText("Date : " + this_meeting.Meeting_Date);
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our text views.
        private ImageButton deletebutton;
        private TextView start_time;
        private TextView place;
        private TextView duration;
        private TextView endtime;
        private TextView withwhom;
        private TextView dateofmeeting;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deletebutton = itemView.findViewById(R.id.deleteButton);
            start_time = itemView.findViewById(R.id.start_time);
            place = itemView.findViewById(R.id.Place);
            duration = itemView.findViewById(R.id.duration);
            endtime = itemView.findViewById(R.id.end_time);
            withwhom = itemView.findViewById(R.id.With_whom);
            dateofmeeting = itemView.findViewById(R.id.dateofmeeting);
        }
    }
}
