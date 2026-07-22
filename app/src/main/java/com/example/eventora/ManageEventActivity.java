package com.example.eventora;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ManageEventActivity extends AppCompatActivity {

    private Button btnAddNewEvent;
    private TextView txtBackToDashboard, txtEmptyEvents;
    private RecyclerView recyclerEvents;
    private EditText edtSearchEvents;
    private Spinner spinnerFilterCategory;
    private DatabaseReference eventReference;
    private ArrayList<EventModel> eventList;
    private ArrayList<EventModel> filteredList;
    private EventAdapter eventAdapter;
    private ArrayList<String> filterCategories;
    private ArrayAdapter<String> filterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        initViews();
        eventReference = FirebaseDatabase.getInstance().getReference("Events");
        setupRecyclerView();
        setupFilterSpinner();
        loadEventsFromFirebase();
        setupSearch();

        btnAddNewEvent.setOnClickListener(view -> {
            Intent intent = new Intent(ManageEventActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        txtBackToDashboard.setOnClickListener(view -> {
            Intent intent = new Intent(ManageEventActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        btnAddNewEvent = findViewById(R.id.btnAddNewEvent);
        txtBackToDashboard = findViewById(R.id.txtBackToDashboard);
        txtEmptyEvents = findViewById(R.id.txtEmptyEvents);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        edtSearchEvents = findViewById(R.id.edtSearchEvents);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
    }

    private void setupRecyclerView() {
        eventList = new ArrayList<>();
        filteredList = new ArrayList<>();
        eventAdapter = new EventAdapter(filteredList, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(EventModel event) {
                showEditEventDialog(event);
            }

            @Override
            public void onDelete(EventModel event) {
                confirmDeleteEvent(event);
            }
        });
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventAdapter);
    }

    private void loadEventsFromFirebase() {
        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    EventModel event = eventSnapshot.getValue(EventModel.class);
                    if (event != null && event.eventId != null) {
                        eventList.add(event);
                    }
                }
                filterEvents();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageEventActivity.this, "Failed to load events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterSpinner() {
        filterCategories = new ArrayList<>();
        filterCategories.add("All Categories");
        filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterCategories);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(filterAdapter);

        FirebaseDatabase.getInstance().getReference("EventCategories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filterCategories.clear();
                filterCategories.add("All Categories");
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String name = categorySnapshot.child("categoryName").getValue(String.class);
                    if (name != null) {
                        filterCategories.add(name);
                    }
                }
                filterAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        edtSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterEvents() {
        String query = edtSearchEvents.getText().toString().toLowerCase().trim();
        String selectedCategory = spinnerFilterCategory.getSelectedItem() != null ? spinnerFilterCategory.getSelectedItem().toString() : "All Categories";

        filteredList.clear();
        for (EventModel event : eventList) {
            boolean matchesSearch = event.eventName.toLowerCase().contains(query);
            boolean matchesCategory = selectedCategory.equals("All Categories") || selectedCategory.equals(event.categoryName);

            if (matchesSearch && matchesCategory) {
                filteredList.add(event);
            }
        }
        eventAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            txtEmptyEvents.setVisibility(View.VISIBLE);
            recyclerEvents.setVisibility(View.GONE);
        } else {
            txtEmptyEvents.setVisibility(View.GONE);
            recyclerEvents.setVisibility(View.VISIBLE);
        }
    }

    private void confirmDeleteEvent(EventModel event) {
        Dialog dialog = new Dialog(ManageEventActivity.this);
        dialog.setContentView(R.layout.dialog_delete_category); // Reusing the same delete dialog layout
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtDeleteMessage = dialog.findViewById(R.id.txtDeleteMessage);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        txtDeleteMessage.setText("Are you sure you want to delete event " + event.eventName + "?");

        btnYes.setOnClickListener(v -> {
            deleteEvent(event.eventId);
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void showEditEventDialog(EventModel event) {
        Dialog dialog = new Dialog(ManageEventActivity.this);
        dialog.setContentView(R.layout.dialog_edit_event);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText edtEventName = dialog.findViewById(R.id.edtEventName);
        Spinner spinnerEventCategory = dialog.findViewById(R.id.spinnerEventCategory);
        EditText edtEventDate = dialog.findViewById(R.id.edtEventDate);
        EditText edtEventTime = dialog.findViewById(R.id.edtEventTime);
        EditText edtVenue = dialog.findViewById(R.id.edtVenue);
        EditText edtDescription = dialog.findViewById(R.id.edtDescription);
        EditText edtRegistrationFee = dialog.findViewById(R.id.edtRegistrationFee);
        EditText edtImageUrl = dialog.findViewById(R.id.edtImageUrl);
        Spinner spinnerEventStatus = dialog.findViewById(R.id.spinnerEventStatus);
        Button btnUpdateEvent = dialog.findViewById(R.id.btnUpdateEvent);

        // Populate fields
        edtEventName.setText(event.eventName);
        edtEventDate.setText(event.eventDate);
        edtEventTime.setText(event.eventTime);
        edtVenue.setText(event.venue);
        edtDescription.setText(event.description);
        edtRegistrationFee.setText(event.registrationFee);
        edtImageUrl.setText(event.imageUrl);

        // Setup Category Spinner
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventCategory.setAdapter(categoryAdapter);

        FirebaseDatabase.getInstance().getReference("EventCategories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int selectedIndex = 0;
                int i = 0;
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String name = categorySnapshot.child("categoryName").getValue(String.class);
                    String id = categorySnapshot.child("categoryId").getValue(String.class);
                    if (name != null) {
                        categories.add(name);
                        if (id != null && id.equals(event.categoryId)) {
                            selectedIndex = i;
                        }
                        i++;
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                spinnerEventCategory.setSelection(selectedIndex);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Setup Status Spinner
        String[] statuses = {"active", "inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventStatus.setAdapter(statusAdapter);
        if ("inactive".equals(event.status)) spinnerEventStatus.setSelection(1);

        // Date and Time Pickers
        edtEventDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> 
                edtEventDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year), 
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        edtEventTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> 
                edtEventTime.setText(String.format("%02d:%02d", hourOfDay, minute)), 
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnUpdateEvent.setOnClickListener(v -> {
            String name = edtEventName.getText().toString().trim();
            if (name.isEmpty()) {
                edtEventName.setError("Required");
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("eventName", name);
            map.put("eventDate", edtEventDate.getText().toString().trim());
            map.put("eventTime", edtEventTime.getText().toString().trim());
            map.put("venue", edtVenue.getText().toString().trim());
            map.put("description", edtDescription.getText().toString().trim());
            map.put("registrationFee", edtRegistrationFee.getText().toString().trim());
            map.put("imageUrl", edtImageUrl.getText().toString().trim());
            map.put("status", spinnerEventStatus.getSelectedItem().toString());

            eventReference.child(event.eventId).updateChildren(map).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    private void deleteEvent(String eventId) {
        eventReference.child(eventId).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(ManageEventActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ManageEventActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}