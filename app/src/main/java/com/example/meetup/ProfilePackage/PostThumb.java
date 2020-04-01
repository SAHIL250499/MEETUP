package com.example.meetup.ProfilePackage;

public class PostThumb {
    public String post_image_thumbnail, timestamp, posted_by, post_image, post_desc;

    public PostThumb(String post_image_thumbnail, String timestamp, String posted_by, String post_image, String post_desc) {
        this.post_image_thumbnail = post_image_thumbnail;
        this.timestamp = timestamp;
        this.posted_by = posted_by;
        this.post_desc = post_desc;
        this.post_image = post_image;
    }

    public PostThumb(){

    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getPost_image_thumbnail() {
        return post_image_thumbnail;
    }

    public void setPost_image_thumbnail(String post_image_thumbnail) {
        this.post_image_thumbnail = post_image_thumbnail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(String posted_by) {
        this.posted_by = posted_by;
    }
}