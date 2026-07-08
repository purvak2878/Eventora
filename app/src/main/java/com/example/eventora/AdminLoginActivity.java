package com.example.eventora;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {
    EditText adminPhone;
    Button adminSendOtp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminPhone = findViewById(R.id.edtAdminPhone);
        adminSendOtp = findViewById(R.id.btnSendOtp);

        adminSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = adminPhone.getText().toString().trim();
                if(phoneNumber.isEmpty()){
                    adminPhone.setError("Mobile number is required !");
                    adminPhone.requestFocus();
                    return;
                }
                showOTPDialog(phoneNumber);
            }
        });
    }
    private void showOTPDialog(String phoneNumber){
        Dialog dialog = new Dialog(AdminLoginActivity.this);
        dialog.setContentView(R.layout.dialog_admin_otp);
        dialog.setCancelable(true);

        TextView OTPPhoneinfo = dialog.findViewById(R.id.edtAdminPhone);
        EditText AdminOTP = dialog.findViewById(R.id.edtAdminOtp);
        Button VerifyOTP = dialog.findViewById(R.id.btnVerifyOtp);

        OTPPhoneinfo.setText("OTP sent to"+phoneNumber);
        VerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = AdminOTP.getText().toString().trim();
                if(otp.isEmpty()){
                    AdminOTP.setError("OTP is required !");
                    AdminOTP.requestFocus();
                    return;
                }
                if(otp.length()<6){
                    AdminOTP.setError("Enter 6 digit OTP!");
                    AdminOTP.requestFocus();
                    return;
                }
                Toast.makeText(getApplicationContext(),"OTP verified!",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
