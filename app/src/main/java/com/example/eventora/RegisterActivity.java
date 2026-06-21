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
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText edtFullName, edtEmail, WhatsappNo, edtPassword;
    Button btnRegister;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        WhatsappNo = findViewById(R.id.WhatsappNo);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");


        TextView txtSignIn = findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerStudent();
            }
        });
    }

    private void registerStudent() {
        String name = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String whatsApp = WhatsappNo.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (name.isEmpty()) {
            edtFullName.setError("Full name is required");
            edtFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Enter a valid email");
            edtEmail.requestFocus();
            return;
        }
        if (whatsApp.isEmpty()) {
            WhatsappNo.setError("Whatsapp number is required");
            WhatsappNo.requestFocus();
            return;
        }
        if (whatsApp.length() < 10) {
            WhatsappNo.setError("Enter a valid WhatsApp number");
            WhatsappNo.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Password should be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        createFirebaseAccount(name, email, whatsApp, password);
    }

    private void createFirebaseAccount(String name, String email, String whatsApp, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        saveStudentData(uid, name, email, whatsApp);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveStudentData(String uid, String name, String email, String whatsApp) {
        HashMap<String, Object> studentMap = new HashMap<>();
        studentMap.put("uid", uid);
        studentMap.put("FullName", name);
        studentMap.put("Email", email);
        studentMap.put("WhatsappNumber", whatsApp);
        studentMap.put("Role", "Student");

        databaseReference.child(uid).setValue(studentMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Data Save Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
