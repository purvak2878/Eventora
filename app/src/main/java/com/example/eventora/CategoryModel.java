package com.example.eventora;

public class CategoryModel {
    public String categoryId;
    public String categoryName;
    public String status;
    public String createdBy;
    public long createdAt;
    public CategoryModel(){

    }
    public CategoryModel(String categoryId,String categoryName,String status,String createdBy,long createdAt){
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}
