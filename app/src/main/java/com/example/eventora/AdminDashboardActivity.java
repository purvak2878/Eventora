package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDashboardActivity extends AppCompatActivity {
    TextView txtAdminWelcome;
    Button btnCreateEvent, btnManageEvents, btnAdminLogout;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        txtAdminWelcome = findViewById(R.id.txtAdminWelcome);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        firebaseAuth = FirebaseAuth.getInstance();
        checkAdminSession();

        btnCreateEvent.setOnClickListener(view -> {
            Toast.makeText(this, "Create Event page will be developed next",
                    Toast.LENGTH_SHORT).show();
        });
        btnManageEvents.setOnClickListener(view -> {
            Toast.makeText(this, "Manage Events page will be developed later",
                    Toast.LENGTH_SHORT).show();
        });
        btnAdminLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            goToAdminLogin();
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
                                txtAdminWelcome.setText("Welcome, " + fullName);
                            } else {
                                txtAdminWelcome.setText("Welcome Admin");
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
