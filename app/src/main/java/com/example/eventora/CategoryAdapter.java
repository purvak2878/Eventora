package com.example.eventora;

import static androidx.recyclerview.widget.RecyclerView.*;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;

import org.w3c.dom.Text;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{
    ArrayList<CategoryModel> categoryList;
    OnCategoryActionListener Listener;

    public interface OnCategoryActionListener{
        void onEdit(CategoryModel category);
        void onDelete(CategoryModel category);
    }
    public CategoryAdapter(ArrayList<CategoryModel> categoryList,OnCategoryActionListener Listener){
        this.categoryList = categoryList;
        this.Listener = Listener;
    }
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_category,parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.txtCategoryName.setText(category.categoryName);
        holder.txtCategoryName.setText("Status :" +category.status);
        holder.txtCategoryId.setText("ID :"+category.categoryId);

        holder.btnEditCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Listener.onEdit(category);
            }
        });
        holder.btnDeleteCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Listener.onDelete(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName,txtCategoryStatus,txtCategoryId;
        Button btnEditCategory,btnDeleteCategory;
        public CategoryViewHolder(@NonNull View itemView){
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtCategoryStatus = itemView.findViewById(R.id.txtCategoryStatus);
            txtCategoryId = itemView.findViewById(R.id.txtCategoryId);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
