package com.example.eventora;

import android.os.Bundle;
import android.widget.TextView;

public class StudentHome extends StudentBaseActivity {

    TextView txtWelcome, txtName, txtEmail, txtWhatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        setupStudentPanel("Home");
    }
    @Override
    protected void setTopBarData(String pageTitle) {
        super.setTopBarData(pageTitle);
        if (txtWelcome != null) txtWelcome.setText("Welcome, " + fullName);
        if (txtName != null) txtName.setText("Name: " + fullName);
        if (txtEmail != null) txtEmail.setText("Email: " + email);
        if (txtWhatsapp != null) txtWhatsapp.setText("WhatsApp: " + whatsappNumber);
    }
}
