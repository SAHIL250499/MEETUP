package com.example.meetup.StoriesPackage;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.meetup.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import xute.storyview.StoryView;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder> {

    private List<Stories> mStoriesList;

    public StoriesAdapter(List<Stories> mStoriesList) {
        this.mStoriesList = mStoriesList;
    }

    @Override
    public StoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stories_single_layout, parent, false);
        return new StoriesViewHolder(v);

    }

    class StoriesViewHolder extends RecyclerView.ViewHolder {
        private StoryView storyView;
        private TextView userNameText;

        StoriesViewHolder(View view) {
            super(view);
            storyView = view.findViewById(R.id.stories_single_storyview);
            userNameText = view.findViewById(R.id.story_single_username);
        }
    }

    @Override
    public void onBindViewHolder(final StoriesViewHolder viewHolder, int i) {
        final Stories stories = mStoriesList.get(i);

        viewHolder.userNameText.setText(stories.getStoryUsername());

//        viewHolder.storyView.resetStoryVisits();
//        viewHolder.storyView.setImageUris(stories.getStoryUriList());
    }

    @Override
    public int getItemCount() {
        return mStoriesList.size();
    }

}

