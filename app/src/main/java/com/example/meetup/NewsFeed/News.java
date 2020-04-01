package com.example.meetup.NewsFeed;

public class News {
    private String date_posted, post_desc, post_image, posted_by, name, img_thumbnail, timestamp, post_type;
    private float xyRatio;

    public News(String date_posted, String post_desc, String post_image, String posted_by, String name, String img_thumbnail, String timestamp, float xyRatio, String post_type) {
        this.date_posted = date_posted;
        this.post_desc = post_desc;
        this.post_image = post_image;
        this.posted_by = posted_by;
        this.name = name;
        this.img_thumbnail = img_thumbnail;
        this.timestamp = timestamp;
        this.xyRatio = xyRatio;
        this.post_type = post_type;
    }

    public News() {
    }

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public float getXyRatio() {
        return xyRatio;
    }

    public void setXyRatio(float xyRatio) {
        this.xyRatio = xyRatio;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate_posted() {
        return date_posted;
    }

    public void setDate_posted(String date_posted) {
        this.date_posted = date_posted;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(String posted_by) {
        this.posted_by = posted_by;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_thumbnail() {
        return img_thumbnail;
    }

    public void setImg_thumbnail(String img_thumbnail) {
        this.img_thumbnail = img_thumbnail;
    }
}
