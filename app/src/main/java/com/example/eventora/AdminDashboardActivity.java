package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDashboardActivity extends AppCompatActivity {
    TextView txtAdminWelcome;
    Button btnCreateEvent,btnHome,btnCategory, btnManageEvents, btnAdminLogout;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtAdminWelcome = findViewById(R.id.txtAdminWelcome);
        btnHome=findViewById(R.id.tabHome);
        btnCategory=findViewById(R.id.tabCategories);
        btnCreateEvent = findViewById(R.id.tabCreateEvent);
        btnManageEvents = findViewById(R.id.tabManageEvents);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        firebaseAuth = FirebaseAuth.getInstance();
        checkAdminSession();
        btnHome.setOnClickListener(view-> {
            Toast.makeText(this,"You are already on Home tab",
                    Toast.LENGTH_SHORT).show();
        });
        btnCategory.setOnClickListener(view->{
            Intent intent = new Intent(AdminDashboardActivity.this,CreateCategoryActivity.class);
            startActivity(intent);
        });
        btnCreateEvent.setOnClickListener(view -> {
            Intent intent = new Intent(AdminDashboardActivity.this,CreateEventAcitivity.class);
            startActivity(intent);
        });
        btnManageEvents.setOnClickListener(view -> {
            Intent intent = new Intent(AdminDashboardActivity.this,ManageEventActivity.class);
            startActivity(intent);
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
