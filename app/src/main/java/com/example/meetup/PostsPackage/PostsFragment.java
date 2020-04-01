package com.example.meetup.PostsPackage;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.meetup.LoginActivity;
import com.example.meetup.NewsFeed.News;
import com.example.meetup.NewsFeed.NewsAdapter;
import com.example.meetup.PostActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {

    private RecyclerView mNewsList;
    private List<News> newsList;
    private NewsAdapter newsAdapter;

    private DatabaseReference mRootRef, mPostsDatabase, mFriendsDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;

    private ExtendedFloatingActionButton newPost;

    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_posts, container, false);

        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList);

        mNewsList = mView.findViewById(R.id.posts_list);
        mNewsList.setHasFixedSize(true);
        mNewsList.setAdapter(newsAdapter);

        newPost = mView.findViewById(R.id.new_post_btn);

        mAuth = FirebaseAuth.getInstance();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = mRootRef.child("Users");
        mFriendsDatabase = mRootRef.child("Friends");
        mPostsDatabase = mRootRef.child("Posts");

        mUsersDatabase.keepSynced(true);
        mFriendsDatabase.keepSynced(true);
        mPostsDatabase.keepSynced(true);

        if (mAuth.getCurrentUser() == null) {
            mAuth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
        } else {
            loadPosts();
        }

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PostActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadPosts() {

        newsList.clear();
        String uid = mAuth.getCurrentUser().getUid();
        mFriendsDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendsSnap) {
                for (DataSnapshot friendsData : friendsSnap.getChildren()) {
                    String f_uid = friendsData.getKey();

                    mPostsDatabase.child(f_uid).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot postsSnap) {
                            for (DataSnapshot PostsData : postsSnap.getChildren()) {

                                final String postType = PostsData.child("post_type").getValue().toString();

                                final String postImage = PostsData.child("post_image").getValue().toString();
                                final String postDesc = PostsData.child("post_desc").getValue().toString();
                                final String datePosted = PostsData.child("date_posted").getValue().toString();
                                final String postedBy = PostsData.child("posted_by").getValue().toString();
                                final String postId = PostsData.child("timestamp").getValue().toString();
                                float xyRatio = 1;

                                if (PostsData.hasChild("width") && PostsData.hasChild("height")) {
                                    int w = Integer.parseInt(PostsData.child("width").getValue().toString());
                                    int h = Integer.parseInt(PostsData.child("height").getValue().toString());
                                    xyRatio = (float) h / w;
                                }

                                final float finalXyRatio = xyRatio;
                                mUsersDatabase.child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String username = dataSnapshot.child("username").getValue().toString();
                                        String profileImg = dataSnapshot.child("img_thumbnail").getValue().toString();

                                        newsList.add(new News(datePosted, postDesc, postImage, postedBy, username.toLowerCase(), profileImg, postId, finalXyRatio, postType));
                                        newsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPostsDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot PostsData : dataSnapshot.getChildren()) {
                    final String postType = PostsData.child("post_type").getValue().toString();
                    final String postImage = PostsData.child("post_image").getValue().toString();
                    final String postDesc = PostsData.child("post_desc").getValue().toString();
                    final String datePosted = PostsData.child("date_posted").getValue().toString();
                    final String postedBy = PostsData.child("posted_by").getValue().toString();
                    final String postId = PostsData.child("timestamp").getValue().toString();
                    float xyRatio = 1;

                    if (PostsData.hasChild("width") && PostsData.hasChild("height")) {
                        int w = Integer.parseInt(PostsData.child("width").getValue().toString());
                        int h = Integer.parseInt(PostsData.child("height").getValue().toString());
                        xyRatio = (float) h / w;
                    }

                    final float finalXyRatio = xyRatio;
                    mUsersDatabase.child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.child("username").getValue().toString();
                            String profileImg = dataSnapshot.child("img_thumbnail").getValue().toString();

                            newsList.add(new News(datePosted, postDesc, postImage, postedBy, username.toLowerCase(), profileImg, postId, finalXyRatio, postType));
                            newsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
