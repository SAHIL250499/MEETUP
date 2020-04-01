package com.example.meetup.ChatsPackage;

public class Chats {

    public String name, status, thumb_image;

    public Chats(String name, String status, String thumb_image) {
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public Chats(){

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