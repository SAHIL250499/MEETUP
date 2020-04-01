package com.example.meetup.LocationPackage;

public class LocUsers {
    private String username, LAT, LNG, profileImage;

    public LocUsers(String username, String LAT, String LNG, String profileImage) {
        this.username = username;
        this.LAT = LAT;
        this.LNG = LNG;
        this.profileImage = profileImage;
    }

    public LocUsers() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLAT() {
        return LAT;
    }

    public void setLAT(String LAT) {
        this.LAT = LAT;
    }

    public String getLNG() {
        return LNG;
    }

    public void setLNG(String LNG) {
        this.LNG = LNG;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
