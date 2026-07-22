package com.example.eventora;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class AdminLoginActivity extends AppCompatActivity {
    EditText adminPhone;
    Button adminSendOtp;
    FirebaseAuth firebaseAuth;
    String VerificationId;
    String adminPhoneNumber;
    PhoneAuthProvider.ForceResendingToken resendToken;
    Dialog otpDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminPhone = findViewById(R.id.edtAdminPhone);
        adminSendOtp = findViewById(R.id.btnSendOtp);
        firebaseAuth = FirebaseAuth.getInstance();

        TextView txtBackToLogin = findViewById(R.id.txtBackToLogin);
        if (txtBackToLogin != null) {
            txtBackToLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        adminSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = adminPhone.getText().toString().trim();
                if(phoneNumber.isEmpty()){
                    adminPhone.setError("Mobile number is required !");
                    adminPhone.requestFocus();
                    return;
                }
                if(phoneNumber.length()==10&& !phoneNumber.startsWith("+")){
                    adminPhoneNumber = "+91"+phoneNumber;
                }else if(phoneNumber.startsWith("+")){
                    adminPhoneNumber = phoneNumber;
                }else{
                    adminPhone.setError("Enter valid phone number");
                    adminPhone.requestFocus();
                    return;
                }
                sendOtp(adminPhoneNumber, resendToken);
            }
        });
    }
    private void sendOtp(String phoneNumber, PhoneAuthProvider.ForceResendingToken token){
        adminSendOtp.setEnabled(false);
        adminSendOtp.setText("Sending OTP..");

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(phoneAuthcallbacks);

        if (token != null) {
            builder.setForceResendingToken(token);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneAuthcallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            adminSendOtp.setEnabled(true);
            adminSendOtp.setText("Send OTP");
            Toast.makeText(AdminLoginActivity.this, "OTP Verified automatically", Toast.LENGTH_LONG).show();
            signInWithPhoneCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            adminSendOtp.setEnabled(true);
            adminSendOtp.setText("Send OTP");
            Toast.makeText(AdminLoginActivity.this, "OTP Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String verificationIdFromFirebase, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            VerificationId = verificationIdFromFirebase;
            resendToken = forceResendingToken;
            adminSendOtp.setEnabled(true);
            adminSendOtp.setText("Resend OTP");
            Toast.makeText(AdminLoginActivity.this, "OTP sent successfully", Toast.LENGTH_LONG).show();
            showOTPDialog(adminPhoneNumber);
        }
    };
    private void showOTPDialog(String phoneNumber){
        if (otpDialog != null && otpDialog.isShowing()) {
            // Update info if dialog is already showing
            TextView OTPPhoneinfo = otpDialog.findViewById(R.id.txtOtpPhoneInfo);
            if (OTPPhoneinfo != null) {
                OTPPhoneinfo.setText("OTP sent to " + phoneNumber);
            }
            return;
        }

        otpDialog = new Dialog(AdminLoginActivity.this);
        otpDialog.setContentView(R.layout.dialog_admin_otp);
        otpDialog.setCancelable(true);

        if (otpDialog.getWindow() != null) {
            otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(otpDialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            otpDialog.getWindow().setAttributes(lp);
        }

        TextView OTPPhoneinfo = otpDialog.findViewById(R.id.txtOtpPhoneInfo);
        EditText AdminOTP = otpDialog.findViewById(R.id.edtAdminOtp);
        Button VerifyOTP = otpDialog.findViewById(R.id.btnVerifyOtp);

        if (OTPPhoneinfo != null) {
            OTPPhoneinfo.setText("OTP sent to " + phoneNumber);
        }
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
                verifyOTP(otp);
            }
        });
        otpDialog.show();
    }
    private void verifyOTP(String otp){
       if(VerificationId==null){
           Toast.makeText(getApplicationContext(),"Please send OTP first",Toast.LENGTH_LONG).show();
           return;
       }
       PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationId,otp);
       signInWithPhoneCredential(credential);
    }
    private void signInWithPhoneCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(authResult -> {
            Toast.makeText(getApplicationContext(), "OTP verified Successfully!", Toast.LENGTH_SHORT).show();
            if (otpDialog != null && otpDialog.isShowing()) {
                otpDialog.dismiss();
            }
            CheckAdminNumberFromDatabase();
        }).addOnFailureListener(e->{
            Toast.makeText(getApplicationContext(), "Invalid OTP!", Toast.LENGTH_SHORT).show();
        });
    }
    private void CheckAdminNumberFromDatabase(){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser==null){
            Toast.makeText(getApplicationContext(), "User not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        String verificationPhoneNumber = currentUser.getPhoneNumber();
        if(verificationPhoneNumber==null || verificationPhoneNumber.isEmpty()){
            firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "Phone number not found!", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference("Admins")
                .child(verificationPhoneNumber)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if(dataSnapshot.exists()){
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                        String role = dataSnapshot.child("role").getValue(String.class);
                        String status = dataSnapshot.child("status").getValue(String.class);

                        if("admin".equals(role)&&"active".equals(status)){
                           Toast.makeText(getApplicationContext(),"Admin login successfully",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AdminLoginActivity.this,AdminDashboardActivity.class);
                            intent.putExtra("adminName",fullName);
                            intent.putExtra("adminPhone",phoneNumber);
                            startActivity(intent);
                            finish();
                        }else{
                            firebaseAuth.signOut();
                            Toast.makeText(getApplicationContext(),"Admin access denied",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        firebaseAuth.signOut();
                        Toast.makeText(getApplicationContext(),"Admin access denied",Toast.LENGTH_LONG).show();
                    }
                });
    }
}
