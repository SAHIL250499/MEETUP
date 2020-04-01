package com.example.meetup.PostsPackage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.meetup.R;
import com.squareup.picasso.Picasso;
import com.theophrast.ui.widget.SquareImageView;

public class PostDetailActivity extends AppCompatActivity {

    SquareImageView postImage;
    TextView postDesc;
    String image_url, post_desc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postImage = findViewById(R.id.post_detail_img);
        postDesc = findViewById(R.id.post_detail_desc);

        image_url = getIntent().getStringExtra("img_url");
        post_desc = getIntent().getStringExtra("post_desc");

        Picasso.with(getApplicationContext()).load(image_url).into(postImage);
        postDesc.setText(post_desc);

    }
}
