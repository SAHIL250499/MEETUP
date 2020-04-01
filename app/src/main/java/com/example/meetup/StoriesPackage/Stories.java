package com.example.meetup.StoriesPackage;

import java.util.ArrayList;

import xute.storyview.StoryModel;

public class Stories {
    private String storyUsername;
//    private ArrayList<StoryModel> storyUriList;

//    public Stories(String storyUsername, ArrayList storyUriList) {
//        this.storyUsername = storyUsername;
//        this.storyUriList = storyUriList;
//    }

    public Stories(String storyUsername) {
        this.storyUsername = storyUsername;
    }

    public Stories() {
    }

    public String getStoryUsername() {
        return storyUsername;
    }

    public void setStoryUsername(String storyUsername) {
        this.storyUsername = storyUsername;
    }

//    public ArrayList getStoryUriList() {
//        return storyUriList;
//    }
//
//    public void setStoryUriList(ArrayList storyUriList) {
//        this.storyUriList = storyUriList;
//    }
}
