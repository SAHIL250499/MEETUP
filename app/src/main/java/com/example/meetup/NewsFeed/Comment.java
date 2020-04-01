package com.example.meetup.NewsFeed;

public class Comment {
    private String comment, date, from, timestamp, username, profile_image, post_id, comment_id, parent_post_id;

    public Comment(String comment, String date, String from, String timestamp, String username, String profile_image, String post_id, String parent_post_id) {
        this.comment = comment;
        this.date = date;
        this.from = from;
        this.timestamp = timestamp;
        this.username = username;
        this.profile_image = profile_image;
        this.post_id = post_id;
        this.parent_post_id = parent_post_id;
    }

    public Comment() {
    }


    public String getParent_post_id() {
        return parent_post_id;
    }

    public void setParent_post_id(String parent_post_id) {
        this.parent_post_id = parent_post_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
