package com.example.meetup.FriendsPackage;


public class Friends {

    public String name, status, thumb_image, request_type;

    public Friends(String name, String status, String thumb_image, String request_type) {
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
        this.request_type = request_type;
    }

    public Friends() {

    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}