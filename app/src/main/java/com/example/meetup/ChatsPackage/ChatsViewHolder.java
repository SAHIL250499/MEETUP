package com.example.meetup.ChatsPackage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsViewHolder extends RecyclerView.ViewHolder {

    TextView chatUserName, chatUserStatus;
    CircleImageView chatUserProfileImage;

    public ChatsViewHolder(@NonNull View itemView) {
        super(itemView);
        chatUserStatus = itemView.findViewById(R.id.posts_single_desc);
        chatUserName = itemView.findViewById(R.id.posts_single_username);
        chatUserProfileImage = itemView.findViewById(R.id.posts_user_single_profile_image);
    }
}