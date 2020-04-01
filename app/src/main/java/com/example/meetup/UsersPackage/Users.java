package com.example.meetup.UsersPackage;

public class Users {
    public String name, status, img_thumbnail, username;

    public Users(String name, String status, String img_thumbnail, String username) {
        this.name = name;
        this.status = status;
        this.img_thumbnail = img_thumbnail;
        this.username = username;
    }

    public Users() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getImg_thumbnail() {
        return img_thumbnail;
    }

    public void setImg_thumbnail(String img_thumbnail) {
        this.img_thumbnail = img_thumbnail;
    }
}
