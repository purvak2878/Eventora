package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public class StudentBaseActivity extends AppCompatActivity {
    TextView btnToggleMenu, txtPageTitle, txtTopStudentName, txtTopRole;
    TextView txtSidebarStudentName, txtSidebarRole;
    TextView menuDashboard, menuBrowseEvents, menuMyEvents, menuQrPass;
    TextView menuAttendance, menuResults, menuCertificates, menuProfile,
            menuLogout;
    ImageView imgProfileIcon;
    FirebaseAuth firebaseAuth;
    View studentSidebar;
    View sidebarOverlay;
    boolean isSidebarVisible = false;
    protected String fullName = "Student";
    protected String email = "";
    protected String whatsappNumber = "";

    protected void setupStudentPanel(String pageTitle) {
        firebaseAuth = FirebaseAuth.getInstance();
        fetchStudentDataFromFirebase(pageTitle);
        connectCommonViews();
        setupSidebarToggle();
        setupMenuClicks();
    }

    private void fetchStudentDataFromFirebase(String pageTitle) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        fullName = snapshot.child("FullName").getValue(String.class);
                        email = snapshot.child("Email").getValue(String.class);
                        whatsappNumber = snapshot.child("WhatsappNumber").getValue(String.class);
                        setTopBarData(pageTitle);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentBaseActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void connectCommonViews() {
        studentSidebar = findViewById(R.id.studentSidebar);
        imgProfileIcon = findViewById(R.id.imgProfileIcon);
        btnToggleMenu = findViewById(R.id.btnToggleMenu);
        txtPageTitle = findViewById(R.id.txtPageTitle);
        txtTopStudentName = findViewById(R.id.txtTopStudentName);
        txtTopRole = findViewById(R.id.txtTopRole);
        txtSidebarStudentName = findViewById(R.id.txtSidebarStudentName);
        txtSidebarRole = findViewById(R.id.txtSidebarRole);
        menuDashboard = findViewById(R.id.menuDashboard);
        menuBrowseEvents = findViewById(R.id.menuBrowseEvents);
        menuMyEvents = findViewById(R.id.menuMyEvents);
        menuQrPass = findViewById(R.id.menuQrPass);
        menuAttendance = findViewById(R.id.menuAttendance);
        menuResults = findViewById(R.id.menuResults);
        menuCertificates = findViewById(R.id.menuCertificates);
        menuProfile = findViewById(R.id.menuProfile);
        menuLogout = findViewById(R.id.menuLogout);
        sidebarOverlay = findViewById(R.id.sidebarOverlay);
    }

    protected void setTopBarData(String pageTitle) {
        if (txtPageTitle != null) txtPageTitle.setText(pageTitle);
        if (txtTopStudentName != null) txtTopStudentName.setText(fullName);
        if (txtTopRole != null) txtTopRole.setText("Student");
        if (txtSidebarStudentName != null) txtSidebarStudentName.setText(fullName);
        if (txtSidebarRole != null) txtSidebarRole.setText("Student Panel");
        
        if (imgProfileIcon != null) {
            imgProfileIcon.setImageResource(R.drawable.dp_icon);
        }
    }

    private void setupSidebarToggle() {
        if (btnToggleMenu != null && studentSidebar != null) {
            btnToggleMenu.setOnClickListener(v -> {
                if (isSidebarVisible) {
                    studentSidebar.setVisibility(View.GONE);
                    if (sidebarOverlay != null) sidebarOverlay.setVisibility(View.GONE);
                } else {
                    studentSidebar.setVisibility(View.VISIBLE);
                    if (sidebarOverlay != null) sidebarOverlay.setVisibility(View.VISIBLE);
                }
                isSidebarVisible = !isSidebarVisible;
            });
        }

        if (sidebarOverlay != null) {
            sidebarOverlay.setOnClickListener(v -> {
                if (isSidebarVisible) {
                    studentSidebar.setVisibility(View.GONE);
                    sidebarOverlay.setVisibility(View.GONE);
                    isSidebarVisible = false;
                }
            });
        }
    }

    private void setupMenuClicks() {
        menuBrowseEvents.setOnClickListener(view-> {
            Intent intent = new Intent(StudentBaseActivity.this,StudentBrowseEventsActivity.class);
            startActivity(intent);
        });
        menuMyEvents.setOnClickListener(view->{
            Toast.makeText(this, "My Events page will open later", Toast.LENGTH_SHORT).show();
        });
        menuQrPass.setOnClickListener(view->{
            Toast.makeText(this, "QR Pass page will open later", Toast.LENGTH_SHORT).show();
        });
        menuAttendance.setOnClickListener(view->{
            Toast.makeText(this, "Attendance page will open later", Toast.LENGTH_SHORT).show();
        });
        menuResults.setOnClickListener(view->{
            Toast.makeText(this, "Results page will open later", Toast.LENGTH_SHORT).show();
        });
        menuCertificates.setOnClickListener(view->{
            Toast.makeText(this, "Certificates page will open later", Toast.LENGTH_SHORT).show();
        });
        menuProfile.setOnClickListener(view->{
            Toast.makeText(this, "Profile page will open later", Toast.LENGTH_SHORT).show();
        });
        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                firebaseAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    protected void passStudentData(Intent intent) {
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("whatsappNumber", whatsappNumber);
    }
}
