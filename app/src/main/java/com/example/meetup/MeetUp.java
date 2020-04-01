package com.example.meetup;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MeetUp extends Application {
    static boolean isInitialized = false;
    @Override
    public void onCreate() {
        super.onCreate();
        if (!isInitialized){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);
        }
    }
}
