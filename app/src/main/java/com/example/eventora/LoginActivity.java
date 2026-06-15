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

public class LoginActivity extends AppCompatActivity {

    EditText LoginEmail, LoginPassword;
    Button LoginButton;
    String userEmail = "Purva@gmail.com";
    String userPassword = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginEmail = findViewById(R.id.edtEmail);
        LoginPassword = findViewById(R.id.edtPassword);
        LoginButton = findViewById(R.id.btnLogin);

        TextView txtNewStudent = findViewById(R.id.txtNewStudent);
        txtNewStudent.setOnClickListener(new View.OnClickListener() {
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
        
        if (email.equals(userEmail) && password.equals(userPassword)) {
            Toast.makeText(this, "Login Successfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
        }
    }
}
