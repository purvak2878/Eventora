package com.example.eventora;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity {

    EditText edtEventName, edtEventDate, edtEventTime, edtVenue;
    EditText edtEventDescription, edtRegistrationFee, edtImageUrl;
    Button btnCreateEvent, btnManageEvent;
    TextView txtBackToDashboard;
    Spinner spinnerEventCategory, spinnerEventStatus;
    NumberPicker numberPickerParticipants;
    FirebaseAuth firebaseAuth;
    DatabaseReference categoryReference, eventsReference;
    ArrayList<CategoryItem> categoryList;
    ArrayAdapter<CategoryItem> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event_acitivity);

        edtEventName = findViewById(R.id.edtEventName);
        edtEventDate = findViewById(R.id.edtEventDate);
        edtEventTime = findViewById(R.id.edtEventTime);
        edtVenue = findViewById(R.id.edtVenue);
        edtEventDescription = findViewById(R.id.edtDescription);
        edtRegistrationFee = findViewById(R.id.edtRegistrationFee);
        edtImageUrl = findViewById(R.id.edtImageUrl);

        spinnerEventCategory = findViewById(R.id.spinnerEventCategory);
        spinnerEventStatus = findViewById(R.id.spinnerEventStatus);

        numberPickerParticipants = findViewById(R.id.numberPickerParticipants);
        numberPickerParticipants.setMinValue(1);
        numberPickerParticipants.setMaxValue(1000);
        numberPickerParticipants.setValue(50);

        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnManageEvent = findViewById(R.id.btnManageEvent);
        txtBackToDashboard = findViewById(R.id.txtBackToDashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        eventsReference = FirebaseDatabase.getInstance().getReference("Events");
        categoryReference = FirebaseDatabase.getInstance().getReference("EventCategories");

        setupCategorySpinner();
        setupStatusSpinner();

        edtEventDate.setOnClickListener(v -> showDatePicker());
        edtEventTime.setOnClickListener(v -> showTimePicker());

        btnCreateEvent.setOnClickListener(view -> {
            validateAndCreateEvent();
        });

        btnManageEvent.setOnClickListener(view -> {
            Intent intent = new Intent(CreateEventActivity.this, ManageEventActivity.class);
            startActivity(intent);
        });

        txtBackToDashboard.setOnClickListener(view -> {
            Intent intent = new Intent(CreateEventActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupStatusSpinner() {
        String[] statuses = {"active", "inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerEventStatus.setAdapter(statusAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            edtEventDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute1);
            edtEventTime.setText(selectedTime);
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void validateAndCreateEvent() {
        String eventName = edtEventName.getText().toString().trim();
        String eventDate = edtEventDate.getText().toString().trim();
        String eventTime = edtEventTime.getText().toString().trim();
        String venue = edtVenue.getText().toString().trim();
        String description = edtEventDescription.getText().toString().trim();
        String registrationFee = edtRegistrationFee.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();
        int maxParticipants = numberPickerParticipants.getValue();
        String status = spinnerEventStatus.getSelectedItem().toString();

        CategoryItem selectedCategory = (CategoryItem) spinnerEventCategory.getSelectedItem();

        if (TextUtils.isEmpty(eventName)) {
            edtEventName.setError("Event name is required");
            return;
        }
        if (selectedCategory == null || TextUtils.isEmpty(selectedCategory.categoryId)) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eventDate)) {
            edtEventDate.setError("Event date is required");
            return;
        }
        if (TextUtils.isEmpty(eventTime)) {
            edtEventTime.setError("Event time is required");
            return;
        }
        if (TextUtils.isEmpty(venue)) {
            edtVenue.setError("Venue is required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            edtEventDescription.setError("Description is required");
            return;
        }
        if (TextUtils.isEmpty(registrationFee)) {
            edtRegistrationFee.setError("Registration fee is required");
            return;
        }
        if (TextUtils.isEmpty(imageUrl)) {
            edtImageUrl.setError("Image URL is required");
            return;
        }

        createEvent(eventName, selectedCategory.categoryId, selectedCategory.categoryName,
                eventDate, eventTime, venue, description, registrationFee, maxParticipants, imageUrl, status);
    }

    private void createEvent(String eventName, String categoryId, String categoryName,
                             String eventDate, String eventTime, String venue, String description,
                             String registrationFee, int maxParticipants, String imageUrl, String status) {

        String eventId = eventsReference.push().getKey();
        if (eventId == null) {
            Toast.makeText(this, "Failed to generate event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        EventModel event = new EventModel(
                eventId,
                eventName,
                categoryId,
                categoryName,
                eventDate,
                eventTime,
                venue,
                description,
                registrationFee,
                maxParticipants,
                imageUrl,
                status,
                System.currentTimeMillis()
        );

        eventsReference.child(eventId).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateEventActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CreateEventActivity.this, "Failed to create event: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCategorySpinner() {
        categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem("", "Select Category"));
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categoryList
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventCategory.setAdapter(categoryAdapter);
        loadCategoriesFromFirebase();
    }

    private void loadCategoriesFromFirebase() {
        categoryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryList.add(new CategoryItem("", "Select Category"));
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryId = categorySnapshot.child("categoryId").getValue(String.class);
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                    String status = categorySnapshot.child("status").getValue(String.class);

                    if (categoryId != null && categoryName != null && "active".equals(status)) {
                        categoryList.add(new CategoryItem(categoryId, categoryName));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                if (categoryList.size() == 1) {
                    Toast.makeText(getApplicationContext(), "No active categories found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load categories!", Toast.LENGTH_LONG).show();
            }
        });
    }

    static class CategoryItem {
        String categoryId;
        String categoryName;

        CategoryItem(String categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        @Override
        public String toString() {
            return categoryName;
        }
    }
}
