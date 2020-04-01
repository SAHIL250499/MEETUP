package com.example.meetup.NewsFeed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.PostsPackage.PostDetailActivity;
import com.example.meetup.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theophrast.ui.widget.SquareImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private DatabaseReference mRootRef, mCommentsDatabase, mUsersDatabase;
    private ImageButton sendComment;
    private EditText commentField;
    private RecyclerView mCommentsList;
    private FirebaseUser mCurrentUser;

    private SquareImageView postImage;
    private TextView postUserName, postUserDesc;

    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private LinearLayoutManager linearLayoutManager;

    private RelativeLayout relativeLayout;

    private String user_id, post_id, postImageUrlThumb, postDesc, postImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        sendComment = findViewById(R.id.comment_send_btn);
        commentField = findViewById(R.id.comment_field);
        mCommentsList = findViewById(R.id.comments_list);

        relativeLayout = findViewById(R.id.card_post);

        postImage = relativeLayout.findViewById(R.id.post_image_card);
        postUserName = relativeLayout.findViewById(R.id.card_post_username);
        postUserDesc = relativeLayout.findViewById(R.id.card_post_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        user_id = getIntent().getStringExtra("user_id");
        post_id = getIntent().getStringExtra("post_id");
        final String current_user = mCurrentUser.getUid();

        commentAdapter = new CommentAdapter(commentList);
        linearLayoutManager = new LinearLayoutManager(this);

        mCommentsList.setHasFixedSize(true);
        mCommentsList.setLayoutManager(linearLayoutManager);

        mCommentsList.setAdapter(commentAdapter);

        mCommentsDatabase = mRootRef.child("Posts/" + user_id + "/" + post_id + "/comments");
        mUsersDatabase = mRootRef.child("Users");

        mRootRef.child("Posts/" + user_id + "/" + post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postImageUrlThumb = dataSnapshot.child("post_image_thumbnail").getValue().toString();
                postImg = dataSnapshot.child("post_image").getValue().toString();
                String postedBy = dataSnapshot.child("posted_by").getValue().toString();
                postDesc = dataSnapshot.child("post_desc").getValue().toString();

                mUsersDatabase.child(postedBy).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("username").getValue().toString();

                        Picasso.with(getApplicationContext()).load(postImageUrlThumb).into(postImage);
                        postUserDesc.setText(postDesc);
                        postUserName.setText(username);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PostDetailActivity.class);
                i.putExtra("img_url", postImg);
                i.putExtra("post_desc", postDesc);
                startActivity(i);
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentField.getText().toString().trim();

                if (!TextUtils.isEmpty(comment)) {

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
                    Date date = new Date();
                    String CURRENT_DATE = formatter.format(date);
                    String CURRENT_TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                    Map commentMap = new HashMap();

                    commentMap.put("comment", comment);
                    commentMap.put("from", current_user);
                    commentMap.put("date", CURRENT_DATE);
                    commentMap.put("timestamp", CURRENT_TIMESTAMP);
                    commentMap.put("likes", 0);

                    mCommentsDatabase.child(CURRENT_TIMESTAMP).setValue(commentMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    commentField.setText("");
                                }
                            });
                }
            }
        });

        loadComments();
    }

    private void loadComments() {
        mCommentsDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot d, @Nullable String s) {
                final String postedBy = d.child("from").getValue().toString();
                final String date = d.child("date").getValue().toString();
                final String comment = d.child("comment").getValue().toString();
                final String timestamp = d.child("timestamp").getValue().toString();

                Log.d("DATASNAP", comment);

                mUsersDatabase.child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profileImage = dataSnapshot.child("img_thumbnail").getValue().toString();

                        commentList.add(new Comment(comment, date, postedBy, timestamp, username, profileImage, post_id, user_id));
                        commentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
