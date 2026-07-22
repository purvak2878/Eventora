package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    TextView txtAdminWelcome;
    TextView txtTotalEvents, txtActiveEvents, txtTotalBookings, txtTotalCategories;
    TextView txtUpcomingEventsEmpty;
    RecyclerView recyclerUpcomingEvents;
    MaterialCardView cardCreateEvent, cardAddCategory, cardManageUsers, cardViewReports;
    ImageView btnAdminLogout;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootReference;
    ArrayList<EventModel> upcomingEventsList;
    EventAdapter upcomingEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        rootReference = FirebaseDatabase.getInstance().getReference();
        initViews();
        firebaseAuth = FirebaseAuth.getInstance();
        checkAdminSession();
        setupStatisticsListeners();
        setupUpcomingEvents();

        cardCreateEvent.setOnClickListener(view -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventActivity.class);
            startActivity(intent);
        });

        cardAddCategory.setOnClickListener(view -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageCategoryActivity.class);
            startActivity(intent);
        });

        cardManageUsers.setOnClickListener(view -> {
            Toast.makeText(this, "Manage Users coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardViewReports.setOnClickListener(view -> {
            Toast.makeText(this, "Reports coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnAdminLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            goToAdminLogin();
        });
    }

    private void initViews() {
        txtAdminWelcome = findViewById(R.id.txtAdminWelcome);
        txtTotalEvents = findViewById(R.id.TotalEventsCount);
        txtActiveEvents = findViewById(R.id.ActiveEventsCount);
        txtTotalBookings = findViewById(R.id.totalBookingsCount);
        txtTotalCategories = findViewById(R.id.totalCategoriesCount);
        txtUpcomingEventsEmpty = findViewById(R.id.txtUpcomingEventsEmpty);
        recyclerUpcomingEvents = findViewById(R.id.recyclerUpcomingEvents);

        cardCreateEvent = findViewById(R.id.cardCreateEvent);
        cardAddCategory = findViewById(R.id.cardAddCategory);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardViewReports = findViewById(R.id.cardViewReports);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);
    }

    private void setupStatisticsListeners() {
        // Events Listener
        rootReference.child("Events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCount = snapshot.getChildrenCount();
                long activeCount = 0;
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String status = eventSnapshot.child("status").getValue(String.class);
                    if ("active".equals(status)) {
                        activeCount++;
                    }
                }
                txtTotalEvents.setText(String.valueOf(totalCount));
                txtActiveEvents.setText(String.valueOf(activeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Categories Listener
        rootReference.child("EventCategories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtTotalCategories.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Bookings Listener
        rootReference.child("Bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtTotalBookings.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupUpcomingEvents() {
        upcomingEventsList = new ArrayList<>();
        upcomingEventsAdapter = new EventAdapter(upcomingEventsList, new EventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(EventModel event) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageEventActivity.class);
                startActivity(intent);
            }

            @Override
            public void onDelete(EventModel event) {}
        });

        recyclerUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerUpcomingEvents.setAdapter(upcomingEventsAdapter);

        rootReference.child("Events").orderByChild("createdAt").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upcomingEventsList.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    EventModel event = eventSnapshot.getValue(EventModel.class);
                    if (event != null) {
                        upcomingEventsList.add(event);
                    }
                }
                Collections.reverse(upcomingEventsList);
                
                if (upcomingEventsList.isEmpty()) {
                    txtUpcomingEventsEmpty.setVisibility(View.VISIBLE);
                    recyclerUpcomingEvents.setVisibility(View.GONE);
                } else {
                    txtUpcomingEventsEmpty.setVisibility(View.GONE);
                    recyclerUpcomingEvents.setVisibility(View.VISIBLE);
                }
                upcomingEventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkAdminSession() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            goToAdminLogin();
            return;
        }
        String phoneNumber = currentUser.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            firebaseAuth.signOut();
            goToAdminLogin();
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference("Admins")
                .child(phoneNumber)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        String role = snapshot.child("role").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);

                        if ("admin".equals(role) && "active".equals(status)) {
                            if (fullName != null && !fullName.isEmpty()) {
                                txtAdminWelcome.setText("Welcome, " + fullName + " 👋!");
                            } else {
                                txtAdminWelcome.setText("Welcome Admin 👋!");
                            }
                        } else {
                            firebaseAuth.signOut();
                            goToAdminLogin();
                        }
                    } else {
                        firebaseAuth.signOut();
                        goToAdminLogin();
                    }
                }).addOnFailureListener(e -> {
                    firebaseAuth.signOut();
                    goToAdminLogin();
                });
    }

    private void goToAdminLogin() {
        Intent intent = new Intent(AdminDashboardActivity.this, AdminLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
