package com.example.eventora;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Locale;

public class StudentBrowseEventsActivity extends AppCompatActivity {
    EditText edtSearchEvent;
    Spinner spinnerStudentCategory;
    TextView txtEmptyEvents;
    RecyclerView recyclerStudentEvents;
    DatabaseReference eventReference;
    DatabaseReference categoryReference;
    ArrayList<EventModel> allEventList;
    ArrayList<EventModel> filteredEventList;
    StudentEventAdapter studentEventAdapter;
    ArrayList<CategoryItem> categoryList;
    ArrayAdapter<CategoryItem> categoryAdapter;
    String selectedCategoryId = "";
    String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_browse_events);

        edtSearchEvent = findViewById(R.id.edtSearchEvent);
        spinnerStudentCategory = findViewById(R.id.spinnerStudentCategory);
        txtEmptyEvents = findViewById(R.id.txtEmptyEvents);
        recyclerStudentEvents = findViewById(R.id.recyclerStudentEvents);

        eventReference = FirebaseDatabase.getInstance().getReference("Events");
        categoryReference = FirebaseDatabase.getInstance().getReference("EventCategories");

        setupRecyclerView();
        setupCategorySpinner();
        setupSearchBox();
        loadCategoriesFromFirebase();
        loadActiveEventsFromFirebase();
    }

    private void setupRecyclerView() {
        allEventList = new ArrayList<>();
        filteredEventList = new ArrayList<>();
        studentEventAdapter = new StudentEventAdapter(filteredEventList);
        recyclerStudentEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudentEvents.setAdapter(studentEventAdapter);
    }

    private void setupCategorySpinner() {
        categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem("", "All Categories"));

        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryList
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudentCategory.setAdapter(categoryAdapter);
        spinnerStudentCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CategoryItem selectedCategory = categoryList.get(position);
                selectedCategoryId = selectedCategory.categoryId;
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupSearchBox() {
        edtSearchEvent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchText = charSequence.toString().trim().toLowerCase(Locale.ROOT);
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void loadCategoriesFromFirebase() {
        categoryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryList.add(new CategoryItem("", "All Categories"));
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryId = categorySnapshot.child("categoryId").getValue(String.class);
                    String categoryName = categorySnapshot.child("categoryName").getValue(String.class);
                    String status = categorySnapshot.child("status").getValue(String.class);

                    if (categoryId != null && categoryName != null && "active".equals(status)) {
                        categoryList.add(new CategoryItem(categoryId, categoryName));
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        StudentBrowseEventsActivity.this,
                        "Failed to load categories",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadActiveEventsFromFirebase() {
        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEventList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String eventId = eventSnapshot.child("eventId").getValue(String.class);
                    String eventName = eventSnapshot.child("eventName").getValue(String.class);
                    String categoryId = eventSnapshot.child("categoryId").getValue(String.class);
                    String categoryName = eventSnapshot.child("categoryName").getValue(String.class);
                    String eventDate = eventSnapshot.child("eventDate").getValue(String.class);
                    String eventTime = eventSnapshot.child("eventTime").getValue(String.class);
                    String description = eventSnapshot.child("description").getValue(String.class);
                    String venue = eventSnapshot.child("venue").getValue(String.class);
                    String registrationFee = eventSnapshot.child("registrationFee").getValue(String.class);
                    String imageUrl = eventSnapshot.child("imageUrl").getValue(String.class);
                    String status = eventSnapshot.child("status").getValue(String.class);
                    Long createdAtValue = eventSnapshot.child("createdAt").getValue(Long.class);
                    long createdAt = createdAtValue != null ? createdAtValue : 0;

                    Long maxParticipantsValue = eventSnapshot.child("maxParticipants").getValue(Long.class);
                    int maxParticipants = maxParticipantsValue != null ? maxParticipantsValue.intValue() : 0;

                    if (eventId != null && eventName != null && "active".equals(status)) {
                        EventModel event = new EventModel(
                                eventId,
                                eventName,
                                categoryId != null ? categoryId : "",
                                categoryName != null ? categoryName : "",
                                eventDate != null ? eventDate : "",
                                eventTime != null ? eventTime : "",
                                venue != null ? venue : "",
                                description != null ? description : "",
                                registrationFee != null ? registrationFee : "Free",
                                maxParticipants,
                                imageUrl != null ? imageUrl : "",
                                status,
                                createdAt
                        );
                        allEventList.add(event);
                    }
                }
                filterEvents();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        StudentBrowseEventsActivity.this,
                        "Failed to load events",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void filterEvents() {
        filteredEventList.clear();
        for (EventModel event : allEventList) {
            boolean categoryMatched = false;
            boolean searchMatched = false;

            if (selectedCategoryId.isEmpty()) {
                categoryMatched = true;
            } else if (event.categoryId != null && event.categoryId.equals(selectedCategoryId)) {
                categoryMatched = true;
            }

            if (searchText.isEmpty()) {
                searchMatched = true;
            } else if (event.eventName != null && event.eventName.toLowerCase(Locale.ROOT).contains(searchText)) {
                searchMatched = true;
            }

            if (categoryMatched && searchMatched) {
                filteredEventList.add(event);
            }
        }
        studentEventAdapter.notifyDataSetChanged();

        if (filteredEventList.isEmpty()) {
            txtEmptyEvents.setVisibility(View.VISIBLE);
            recyclerStudentEvents.setVisibility(View.GONE);
        } else {
            txtEmptyEvents.setVisibility(View.GONE);
            recyclerStudentEvents.setVisibility(View.VISIBLE);
        }
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
