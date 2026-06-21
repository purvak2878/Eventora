package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText LoginEmail, LoginPassword;
    Button LoginButton;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginEmail = findViewById(R.id.edtEmail);
        LoginPassword = findViewById(R.id.edtPassword);
        LoginButton = findViewById(R.id.btnLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        TextView txtRegisterHere = findViewById(R.id.txtRegisterHere);
        txtRegisterHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = LoginEmail.getText().toString().trim();
        String password = LoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            LoginEmail.setError("Email is required");
            LoginEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            LoginEmail.setError("Enter a valid Email");
            LoginEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            LoginPassword.setError("Password is required");
            LoginPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            LoginPassword.setError("Password must be at least 6 characters");
            LoginPassword.requestFocus();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        fetchStudentData(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void fetchStudentData(String uid) {
        databaseReference.child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("FullName").getValue(String.class);
                        String email = snapshot.child("Email").getValue(String.class);
                        String whatsappNumber = snapshot.child("WhatsappNumber").getValue(String.class);
                        String role = snapshot.child("Role").getValue(String.class);

                        if (role != null && role.equalsIgnoreCase("Student")) {
                            Intent intent = new Intent(LoginActivity.this, StudentHome.class);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("email", email);
                            intent.putExtra("whatsappNumber", whatsappNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Access denied. Student account required.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Data Fetch Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
