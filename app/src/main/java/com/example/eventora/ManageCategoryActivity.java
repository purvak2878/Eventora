package com.example.eventora;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.HashMap;

public class ManageCategoryActivity extends AppCompatActivity {

    private Button btnAddNewCategory;
    private TextView txtBackToDashboard, txtEmptyCategories;
    private RecyclerView recyclerCategories;
    private DatabaseReference categoryReference;
    private ArrayList<CategoryModel> categoryList;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_category);

        initViews();
        categoryReference = FirebaseDatabase.getInstance().getReference("EventCategories");
        setupRecyclerView();
        loadCategoriesFromFirebase();

        btnAddNewCategory.setOnClickListener(view -> {
            Intent intent = new Intent(ManageCategoryActivity.this, CreateCategoryActivity.class);
            startActivity(intent);
        });

        txtBackToDashboard.setOnClickListener(view -> {
            Intent intent = new Intent(ManageCategoryActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        btnAddNewCategory = findViewById(R.id.btnAddNewCategory);
        txtBackToDashboard = findViewById(R.id.txtBackToDashboard);
        txtEmptyCategories = findViewById(R.id.txtEmptyCategories);
        recyclerCategories = findViewById(R.id.recyclerCategories);
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CategoryModel category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDelete(CategoryModel category) {
                confirmDeleteCategory(category);
            }
        });
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategories.setAdapter(categoryAdapter);
    }

    private void loadCategoriesFromFirebase() {
        categoryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    CategoryModel category = categorySnapshot.getValue(CategoryModel.class);
                    if (category != null && category.categoryId != null) {
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCategoryActivity.this, "Failed to load categories: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (categoryList.isEmpty()) {
            txtEmptyCategories.setVisibility(View.VISIBLE);
            recyclerCategories.setVisibility(View.GONE);
        } else {
            txtEmptyCategories.setVisibility(View.GONE);
            recyclerCategories.setVisibility(View.VISIBLE);
        }
    }

    private void showEditCategoryDialog(CategoryModel category) {
        Dialog dialog = new Dialog(ManageCategoryActivity.this);
        dialog.setContentView(R.layout.dialog_edit_category);
        dialog.setCancelable(true);

        EditText edtCategoryName = dialog.findViewById(R.id.edtCategoryName);
        Spinner spinnerEditCategoryStatus = dialog.findViewById(R.id.spinnerEditCategoryStatus);
        Button btnUpdateCategory = dialog.findViewById(R.id.btnUpdateCategory);

        edtCategoryName.setText(category.categoryName);

        String[] statusList = {"active", "inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCategoryStatus.setAdapter(statusAdapter);

        if ("inactive".equals(category.status)) {
            spinnerEditCategoryStatus.setSelection(1);
        } else {
            spinnerEditCategoryStatus.setSelection(0);
        }

        btnUpdateCategory.setOnClickListener(view -> {
            String updatedCategoryName = edtCategoryName.getText().toString().trim();
            String updatedStatus = spinnerEditCategoryStatus.getSelectedItem().toString();

            if (updatedCategoryName.isEmpty()) {
                edtCategoryName.setError("Category name is required");
                edtCategoryName.requestFocus();
                return;
            }

            updateCategory(category.categoryId, updatedCategoryName, updatedStatus, dialog);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void updateCategory(String categoryId, String categoryName, String status, Dialog dialog) {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("categoryName", categoryName);
        updateMap.put("status", status);
        updateMap.put("updatedAt", System.currentTimeMillis());

        categoryReference.child(categoryId).updateChildren(updateMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ManageCategoryActivity.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageCategoryActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeleteCategory(CategoryModel category) {
        Dialog dialog = new Dialog(ManageCategoryActivity.this);
        dialog.setContentView(R.layout.dialog_delete_category);
        dialog.setCancelable(true);

        TextView txtDeleteMessage = dialog.findViewById(R.id.txtDeleteMessage);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        txtDeleteMessage.setText("Are you sure you want to delete " + category.categoryName + "?");

        btnYes.setOnClickListener(v -> {
            deleteCategory(category.categoryId);
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void deleteCategory(String categoryId) {
        categoryReference.child(categoryId).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(ManageCategoryActivity.this, "Category deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ManageCategoryActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
