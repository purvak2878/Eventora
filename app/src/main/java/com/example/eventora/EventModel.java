package com.example.eventora;

public class EventModel {
    public String eventId;
    public String eventName;
    public String categoryId;
    public String categoryName;
    public String eventDate;
    public String eventTime;
    public String venue;
    public String description;
    public String registrationFee;
    public int maxParticipants;
    public String imageUrl;
    public String status;
    public long createdAt;

    public EventModel() {
        // Default constructor required for calls to DataSnapshot.getValue(EventModel.class)
    }

    public EventModel(String eventId, String eventName, String categoryId, String categoryName, 
                      String eventDate, String eventTime, String venue, String description, 
                      String registrationFee, int maxParticipants, String imageUrl, String status, long createdAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.description = description;
        this.registrationFee = registrationFee;
        this.maxParticipants = maxParticipants;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }
}
