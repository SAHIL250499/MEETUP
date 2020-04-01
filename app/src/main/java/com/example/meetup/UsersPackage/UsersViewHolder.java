package com.example.meetup.UsersPackage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersViewHolder extends RecyclerView.ViewHolder{
    TextView userName, userStatus;
    CircleImageView profileImage;

    UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.posts_single_username);
        userStatus = itemView.findViewById(R.id.posts_single_desc);
        profileImage = itemView.findViewById(R.id.posts_user_single_profile_image);
    }
}