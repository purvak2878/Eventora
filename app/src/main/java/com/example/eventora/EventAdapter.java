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
        holder.txtEventDateTime.setText("Date & Time: " + event.eventDate + " | " + event.eventTime);
        holder.txtEventStatus.setText("Status: " + event.status);

        holder.btnEditEvent.setOnClickListener(v -> listener.onEdit(event));
        holder.btnDeleteEvent.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName, txtEventCategory, txtEventDateTime, txtEventStatus;
        ImageView btnEditEvent, btnDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtEventCategory = itemView.findViewById(R.id.txtEventCategory);
            txtEventDateTime = itemView.findViewById(R.id.txtEventDateTime);
            txtEventStatus = itemView.findViewById(R.id.txtEventStatus);
            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}