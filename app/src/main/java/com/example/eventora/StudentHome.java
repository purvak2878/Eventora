package com.example.eventora;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class StudentHome extends AppCompatActivity {

    TextView txtWelcome, txtName, txtEmail, txtWhatsapp;
    Button btnLogout;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

        txtWelcome = findViewById(R.id.txtWelcome);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtWhatsapp = findViewById(R.id.txtWhatsapp);
        btnLogout = findViewById(R.id.btnLogout);
        firebaseAuth = FirebaseAuth.getInstance();

        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String whatsappNumber = getIntent().getStringExtra("whatsappNumber");
        txtWelcome.setText("Welcome, " + fullName);
        txtName.setText("Name: " + fullName);
        txtEmail.setText("Email: " + email);
        txtWhatsapp.setText("WhatsApp: " + whatsappNumber);

        btnLogout.setOnClickListener(view-> {
            firebaseAuth.signOut();
            Intent intent = new Intent(StudentHome.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}