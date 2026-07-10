package com.example.eventora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateCategoryActivity extends AppCompatActivity {
    EditText edtCategoryName;
    Spinner spinnerCategoryStatus;
    Button btnAddCategory;
    TextView txtBackToAdminDashboard;
    FirebaseAuth firebaseAuth;
    DatabaseReference categoryReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtCategoryName = findViewById(R.id.edtCategoryName);
        spinnerCategoryStatus = findViewById(R.id.spinnerCategoryStatus);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        txtBackToAdminDashboard = findViewById(R.id.txtBackToAdminDashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        categoryReference = FirebaseDatabase.getInstance().getReference("EventCategories");

        setupStatusSpinner();

        btnAddCategory.setOnClickListener(view -> {
            validateAndAddCategory();
        });

        txtBackToAdminDashboard.setOnClickListener(view -> {
            Intent intent = new Intent(CreateCategoryActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupStatusSpinner() {
        String[] statusList = {"active", "inactive"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, statusList
        );
        spinnerCategoryStatus.setAdapter(adapter);
    }

    private void validateAndAddCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();
        String status = spinnerCategoryStatus.getSelectedItem().toString();

        if (categoryName.isEmpty()) {
            edtCategoryName.setError("Category name is required");
            edtCategoryName.requestFocus();
            return;
        }

        addCategory(categoryName, status);
    }

    private void addCategory(String categoryName, String status) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Admin session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        String createdBy = currentUser.getPhoneNumber();
        if (createdBy == null || createdBy.isEmpty()) {
            createdBy = currentUser.getEmail();
        }

        String categoryId = categoryReference.push().getKey();
        if (categoryId == null) {
            Toast.makeText(this, "Failed to generate category ID", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddCategory.setEnabled(false);
        btnAddCategory.setText("Adding Category...");

        HashMap<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("categoryId", categoryId);
        categoryMap.put("categoryName", categoryName);
        categoryMap.put("status", status);
        categoryMap.put("createdBy", createdBy);
        categoryMap.put("createdAt", System.currentTimeMillis());

        categoryReference.child(categoryId).setValue(categoryMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CreateCategoryActivity.this, "Category added successfully", Toast.LENGTH_LONG).show();
                    edtCategoryName.setText("");
                    btnAddCategory.setEnabled(true);
                    btnAddCategory.setText("Add Category");
                })
                .addOnFailureListener(e -> {
                    btnAddCategory.setEnabled(true);
                    btnAddCategory.setText("Add Category");
                    Toast.makeText(CreateCategoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
