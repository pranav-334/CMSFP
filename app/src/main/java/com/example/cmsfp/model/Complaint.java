package com.example.cmsfp.model;

public class Complaint {
    String id, userName, department, status, reply, description,image, complaintId;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Complaint() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Complaint(String id, String userName, String department, String status, String reply, String description) {
        this.id = id;
        this.userName = userName;
        this.department = department;
        this.status = status;
        this.reply = reply;
        this.description = description;
    }
}
