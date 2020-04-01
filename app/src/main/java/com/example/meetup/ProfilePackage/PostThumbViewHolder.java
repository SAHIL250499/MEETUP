package com.example.meetup.ProfilePackage;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;
import com.theophrast.ui.widget.SquareImageView;

class PostThumbViewHolder extends RecyclerView.ViewHolder {

    public SquareImageView postThumbView;

    public PostThumbViewHolder(View mView) {
        super(mView);
        postThumbView = mView.findViewById(R.id.post_thumb_single_image);
    }
}
