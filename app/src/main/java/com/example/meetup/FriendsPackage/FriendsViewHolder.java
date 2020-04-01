package com.example.meetup.FriendsPackage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public TextView userName, userStatus;
    public CircleImageView userImage;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);
        userStatus = itemView.findViewById(R.id.posts_single_desc);
        userName = itemView.findViewById(R.id.posts_single_username);
        userImage = itemView.findViewById(R.id.posts_user_single_profile_image);
    }
}
