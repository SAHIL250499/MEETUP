package com.example.meetup.LocationPackage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocationUserAdapter extends RecyclerView.Adapter<LocationUserAdapter.LocationViewHolder> {

    private List<LocUsers> mLocList;

    public LocationUserAdapter(List<LocUsers> mLocList) {
        this.mLocList = mLocList;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout, parent, false);
        return new LocationViewHolder(v);
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView displayName, displayStatus;

        LocationViewHolder(View view) {
            super(view);
            profileImage = view.findViewById(R.id.posts_user_single_profile_image);
            displayStatus = view.findViewById(R.id.posts_single_desc);
            displayName = view.findViewById(R.id.posts_single_username);
        }
    }

    @Override
    public void onBindViewHolder(final LocationViewHolder viewHolder, int i) {
        LocUsers locUsers = mLocList.get(i);
        viewHolder.displayName.setText(locUsers.getUsername());
        viewHolder.displayStatus.setText(locUsers.getUsername());
        Picasso.with(viewHolder.profileImage.getContext()).load(locUsers.getProfileImage()).into(viewHolder.profileImage);
    }

    @Override
    public int getItemCount() {
        return mLocList.size();
    }
}

