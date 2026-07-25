package com.example.eventora;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<EventModel> eventList;
    private OnEventActionListener listener;

    public interface OnEventActionListener {
        void onEdit(EventModel event);
        void onDelete(EventModel event);
    }

    public EventAdapter(ArrayList<EventModel> eventList, OnEventActionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.txtEventName.setText(event.eventName);
        holder.txtEventCategory.setText("Category: " + event.categoryName);
        holder.txtEventDate.setText("Date: " + event.eventDate);
        holder.txtEventTime.setText("Time: " + event.eventTime);
        holder.txtRegistrationFee.setText("Fee: " + event.registrationFee);
        holder.txtMaxParticipants.setText("Max Participants: " + event.maxParticipants);
        holder.txtEventStatus.setText("Status: " + event.status);

        holder.btnEditEvent.setOnClickListener(v -> listener.onEdit(event));
        holder.btnDeleteEvent.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName, txtEventCategory, txtEventDate, txtEventTime, txtRegistrationFee, txtMaxParticipants, txtEventStatus;
        ImageView btnEditEvent, btnDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtEventCategory = itemView.findViewById(R.id.txtEventCategory);
            txtEventDate = itemView.findViewById(R.id.txtEventDate);
            txtEventTime = itemView.findViewById(R.id.txtEventTime);
            txtRegistrationFee = itemView.findViewById(R.id.txtRegistrationFee);
            txtMaxParticipants = itemView.findViewById(R.id.txtMaxParticipants);
            txtEventStatus = itemView.findViewById(R.id.txtEventStatus);
            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}