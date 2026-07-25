package com.example.eventora;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentEventAdapter extends RecyclerView.Adapter<StudentEventAdapter.StudentEventViewHolder> {

    ArrayList<EventModel> eventList;

    public StudentEventAdapter(ArrayList<EventModel> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public StudentEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_event, parent, false);
        return new StudentEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentEventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.txtStudentEventName.setText(event.eventName);
        holder.txtStudentEventCategory.setText("Category: " + event.categoryName);
        holder.txtStudentEventDateTime.setText("Date: " + event.eventDate + " | Time: " + event.eventTime);
        holder.txtStudentEventFee.setText("Fee: " + event.registrationFee);
        holder.txtStudentEventParticipants.setText("Max Participants: " + event.maxParticipants);
        holder.txtStudentEventDescription.setText(event.description);

        holder.btnViewEventDetails.setOnClickListener(view -> {
            Toast.makeText(
                    view.getContext(),
                    "Event details page will be developed later",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class StudentEventViewHolder extends RecyclerView.ViewHolder {
        TextView txtStudentEventName, txtStudentEventCategory, txtStudentEventDateTime;
        TextView txtStudentEventFee, txtStudentEventParticipants, txtStudentEventDescription;
        Button btnViewEventDetails;

        public StudentEventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStudentEventName = itemView.findViewById(R.id.txtStudentEventName);
            txtStudentEventCategory = itemView.findViewById(R.id.txtStudentEventCategory);
            txtStudentEventDateTime = itemView.findViewById(R.id.txtStudentEventDateTime);
            txtStudentEventFee = itemView.findViewById(R.id.txtStudentEventFee);
            txtStudentEventParticipants = itemView.findViewById(R.id.txtStudentEventParticipants);
            txtStudentEventDescription = itemView.findViewById(R.id.txtStudentEventDescription);
            btnViewEventDetails = itemView.findViewById(R.id.btnViewEventDetails);
        }
    }
}
