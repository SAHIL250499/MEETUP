package com.example.meetup.ProfilePackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.ChatsPackage.ChatActivity;
import com.example.meetup.DetailsActivity;
import com.example.meetup.FriendsPackage.FriendsListActivity;
import com.example.meetup.NewsFeed.CommentActivity;
import com.example.meetup.PostsPackage.PostDetailActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {
    private TextView mDisplayName, mDisplayStatus, mNoOfPosts, mNoOfFriends, mOnlineStatus, mLastSeen;
    private CircleImageView mDisplayProfileImg;
    private MaterialButton mProfileSendReq, mProfileSendMessage;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private RecyclerView mPostThumbList;
    private LinearLayout friendsLayout;

    private Toolbar mToolbar;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mPostsDatabase;
    private DatabaseReference mNotificationsDatabase;

    private ProgressDialog mProgressDialog;

    private FirebaseRecyclerAdapter<PostThumb, PostThumbViewHolder> adapter;
    private FirebaseRecyclerOptions<PostThumb> options;

    private String CURRENT_STATE;

    private String NOT_FRIENDS = "not_friends";
    private String REQUEST_TYPE = "request_type";
    private String SENT = "sent";
    private String RECEIVED = "received";
    private String REQUEST_SENT = "request_sent";
    private String REQUEST_RECEIVED = "request_received";
    private String FRIENDS = "friends";
    private String SELF = "self";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName = findViewById(R.id.profile_userName);
        mDisplayStatus = findViewById(R.id.profile_userStatus);
        mNoOfFriends = findViewById(R.id.no_of_friends);
        mNoOfPosts = findViewById(R.id.no_of_posts);
        mOnlineStatus = findViewById(R.id.last_seen);
        mLastSeen = findViewById(R.id.last_seen_tv);

        friendsLayout = findViewById(R.id.friends_prof_layout);

        mPostThumbList = findViewById(R.id.messages_list);

        mDisplayProfileImg = findViewById(R.id.profile_user_image);

        mProfileSendMessage = findViewById(R.id.profile_sendMessage);
        mProfileSendReq = findViewById(R.id.profile_friendReq);

        mToolbar = findViewById(R.id.profile_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostThumbList = findViewById(R.id.post_thumb_list);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user's data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        CURRENT_STATE = "not_friends";

        // GET String From Intent
        final String uid = getIntent().getStringExtra("user_id");

        if (uid.equals(mAuth.getCurrentUser().getUid())) {
            CURRENT_STATE = SELF;
            mProfileSendReq.setText("Edit Profile");
            mProfileSendReq.setStrokeColorResource(R.color.colorAccent);
            mProfileSendReq.setRippleColorResource(R.color.colorDanger);
            mProfileSendReq.setTextColor(getResources().getColor(R.color.colorAccent));
            mProfileSendMessage.setVisibility(View.GONE);
        }

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mPostsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), FriendsListActivity.class);
                i.putExtra("friends_uid", uid);
                startActivity(i);
            }
        });

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    final String userImage = dataSnapshot.child("img_thumbnail").getValue().toString();
                    String online_date = dataSnapshot.child("online_at").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();

                    getSupportActionBar().setTitle(username);

                    mProfileSendMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                            chatIntent.putExtra("user_id", uid);
                            chatIntent.putExtra("user_name", userName);
                            startActivity(chatIntent);
                        }
                    });

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
                    Date date = new Date();
                    String CURRENT_DATE = formatter.format(date);

                    String online_status = getDaysBtwDates(online_date, CURRENT_DATE);

                    if (online_status.equals("1m")) {
                        mOnlineStatus.setText("now");
                        mOnlineStatus.setTextColor(Color.parseColor("#0bff07"));
                        mLastSeen.setText("last seen");
                    } else {
                        mOnlineStatus.setText(online_status);
                        mOnlineStatus.setTextColor(getResources().getColor(R.color.colorBlack));
                        mLastSeen.setText("last seen");
                    }

                    mDisplayName.setText(userName);
                    mDisplayStatus.setText(userStatus);

                    if (!userImage.equals("default")) {
                        Picasso.with(ProfileActivity.this).load(userImage)
                                .placeholder(R.drawable.default_img)
                                .into(mDisplayProfileImg);
                    } else {
                        Picasso.with(ProfileActivity.this)
                                .load(R.drawable.default_img)
                                .into(mDisplayProfileImg);
                    }

                    mFriendDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String friends_count = String.valueOf(dataSnapshot.getChildrenCount());
                            mNoOfFriends.setText(friends_count);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mPostsDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String posts_count = String.valueOf(dataSnapshot.getChildrenCount());
                            mNoOfPosts.setText(posts_count);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                // FRIENDS LIST / REQUEST FEATURE
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(uid)) {
                            String req_type = dataSnapshot.child(uid).child(REQUEST_TYPE).getValue().toString();
                            if (req_type.equals(RECEIVED)) {
                                CURRENT_STATE = REQUEST_RECEIVED;
                                mProfileSendMessage.setVisibility(View.GONE);
                                mProfileSendReq.setText("Accept Request");
                                mProfileSendReq.setStrokeColorResource(R.color.colorInfo);
                                mProfileSendReq.setTextColor(getResources().getColor(R.color.colorInfo));
                                mProfileSendReq.setRippleColorResource(R.color.colorInfo);
                                mProfileSendReq.setVisibility(View.VISIBLE);
                            } else if (req_type.equals(SENT)) {
                                CURRENT_STATE = REQUEST_SENT;
                                mProfileSendMessage.setVisibility(View.GONE);
                                mProfileSendReq.setText("Cancel Request");
                                mProfileSendReq.setStrokeColorResource(R.color.colorDanger);
                                mProfileSendReq.setTextColor(getResources().getColor(R.color.colorDanger));
                                mProfileSendReq.setRippleColorResource(R.color.colorDanger);
                                mProfileSendReq.setVisibility(View.VISIBLE);
                            }
                        }

                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(uid)) {
                            CURRENT_STATE = FRIENDS;
                            mProfileSendReq.setText("Unfriend");
                            mProfileSendReq.setStrokeColorResource(R.color.colorWarning);
                            mProfileSendReq.setTextColor(getResources().getColor(R.color.colorWarning));
                            mProfileSendReq.setRippleColorResource(R.color.colorWarning);
                            mProfileSendReq.setVisibility(View.VISIBLE);
                            mProfileSendMessage.setVisibility(View.VISIBLE);
                        }
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

        mProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReq.setEnabled(false);

                // NOT FRIENDS STATE

                if (CURRENT_STATE.equals(NOT_FRIENDS) && !mCurrentUser.getUid().equals(uid)) {
                    mProfileSendReq.setVisibility(View.VISIBLE);
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).child(REQUEST_TYPE).setValue(SENT)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).child(REQUEST_TYPE).setValue(RECEIVED)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mProfileSendReq.setEnabled(true);
                                                        CURRENT_STATE = REQUEST_SENT;
                                                        mProfileSendMessage.setVisibility(View.GONE);
                                                        mProfileSendReq.setText("Cancel Request");
                                                        mProfileSendReq.setStrokeColorResource(R.color.colorDanger);
                                                        mProfileSendReq.setTextColor(getResources().getColor(R.color.colorDanger));
                                                        mProfileSendReq.setRippleColorResource(R.color.colorDanger);
                                                        mProfileSendReq.setVisibility(View.VISIBLE);

                                                        Map notifMap = new HashMap();
                                                        notifMap.put("from", mCurrentUser.getUid());
                                                        notifMap.put("type", "request");

                                                        mNotificationsDatabase.child("Requests").child(uid).push().setValue(notifMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toasty.success(getApplicationContext(), "Request Sent", Toast.LENGTH_SHORT, true).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                    } else {
                                        Toasty.error(getApplicationContext(), "Failed to send request", Toast.LENGTH_SHORT, true).show();
                                    }
                                }
                            });
                }

                if (CURRENT_STATE.equals(REQUEST_SENT)) {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CURRENT_STATE = NOT_FRIENDS;
                                    mProfileSendReq.setText("Add Friend");
                                    mProfileSendMessage.setVisibility(View.GONE);
                                    mProfileSendReq.setStrokeColorResource(R.color.colorInfo);
                                    mProfileSendReq.setTextColor(getResources().getColor(R.color.colorInfo));
                                    mProfileSendReq.setRippleColorResource(R.color.colorInfo);
                                    mProfileSendReq.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }

                mProfileSendReq.setEnabled(true);

                if (CURRENT_STATE.equals(REQUEST_RECEIVED)) {
                    final String CURRENT_DATE = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(uid).child("date").setValue(CURRENT_DATE).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(uid).child(mCurrentUser.getUid()).child("date").setValue(CURRENT_DATE).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    CURRENT_STATE = FRIENDS;
                                                    mProfileSendReq.setText("Unfriend");
                                                    mProfileSendReq.setStrokeColorResource(R.color.colorWarning);
                                                    mProfileSendReq.setTextColor(getResources().getColor(R.color.colorWarning));
                                                    mProfileSendReq.setRippleColorResource(R.color.colorWarning);
                                                    mProfileSendReq.setVisibility(View.VISIBLE);
                                                    mProfileSendMessage.setVisibility(View.VISIBLE);

                                                    FirebaseDatabase.getInstance().getReference().child("Notifications/Requests/" + mCurrentUser.getUid()).removeValue();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                }

                if (CURRENT_STATE.equals(FRIENDS)) {
                    mFriendDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CURRENT_STATE = NOT_FRIENDS;
                                    mProfileSendReq.setText("Add Friend");
                                    mProfileSendReq.setStrokeColorResource(R.color.colorInfo);
                                    mProfileSendReq.setTextColor(getResources().getColor(R.color.colorInfo));
                                    mProfileSendReq.setRippleColorResource(R.color.colorInfo);
                                    mProfileSendReq.setVisibility(View.VISIBLE);
                                    mProfileSendMessage.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }

                if (CURRENT_STATE.equals(SELF)) {
                    startActivity(new Intent(ProfileActivity.this, DetailsActivity.class));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final String uid = getIntent().getStringExtra("user_id");

        options = new FirebaseRecyclerOptions.Builder<PostThumb>()
                .setQuery(mPostsDatabase.child(uid), PostThumb.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<PostThumb, PostThumbViewHolder>(options) {

            @Override
            public PostThumb getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }

            @Override
            protected void onBindViewHolder(final PostThumbViewHolder postThumbViewHolder, int i, final PostThumb postThumb) {

                Picasso.with(getApplicationContext()).load(postThumb.getPost_image_thumbnail())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_img)
                        .into(postThumbViewHolder.postThumbView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ProfileActivity.this).load(postThumb.getPost_image_thumbnail())
                                        .placeholder(R.drawable.default_img)
                                        .into(postThumbViewHolder.postThumbView);
                            }
                        });

                final String pid = getRef(getItemCount() - 1 - i).getKey();
                final String uid = getRef(getItemCount() - 1 - i).getParent().getKey();

                postThumbViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(postThumbViewHolder.itemView.getContext(), CommentActivity.class);
                        i.putExtra("user_id", postThumb.getPosted_by());
                        i.putExtra("post_id", postThumb.getTimestamp());
                        startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public PostThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.thumb_post_single_layout, parent, false);

                return new PostThumbViewHolder(mView);
            }
        };
        mPostThumbList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    @Override
    protected void onResume() {
        super.onResume();
        final String uid = getIntent().getStringExtra("user_id");

        options = new FirebaseRecyclerOptions.Builder<PostThumb>()
                .setQuery(mPostsDatabase.child(uid), PostThumb.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<PostThumb, PostThumbViewHolder>(options) {

            @Override
            public PostThumb getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }

            @Override
            protected void onBindViewHolder(final PostThumbViewHolder postThumbViewHolder, int i, final PostThumb postThumb) {
                Picasso.with(getApplicationContext()).load(postThumb.getPost_image_thumbnail()).into(postThumbViewHolder.postThumbView);

                final String pid = getRef(getItemCount() - 1 - i).getKey();
                final String uid = getRef(getItemCount() - 1 - i).getParent().getKey();

                postThumbViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(postThumbViewHolder.itemView.getContext(), CommentActivity.class);
                        i.putExtra("user_id", postThumb.getPosted_by());
                        i.putExtra("post_id", postThumb.getTimestamp());
                        startActivity(i);
                    }
                });
            }

            @NonNull
            @Override
            public PostThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.thumb_post_single_layout, parent, false);

                return new PostThumbViewHolder(mView);
            }
        };
        mPostThumbList.setAdapter(adapter);
        adapter.startListening();
    }

    private String getDaysBtwDates(String date1, String date2) {
        long days;

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");

        try {
            Date dt1 = myFormat.parse(date1);
            Date dt2 = myFormat.parse(date2);

            long diff = dt2.getTime() - dt1.getTime();
            days = diff / 1000L / 60L / 60L / 24L;

            if (days == 0) {
                long diff_m = (dt2.getTime() - dt1.getTime()) / 1000 / 60;

                if (diff_m == 0) {
                    return "1m";
                } else if (diff_m >= 60) {
                    return (int) (diff_m / 60) + "h";
                } else {
                    return diff_m + "m";
                }
            } else if (days > 0 && days < 7) {
                return (int) (days) + "d";
            } else if (days >= 7 && days <= 29) {
                return (int) (days / 7) + "w";
            } else if (days >= 30 && days <= 364) {
                return (int) (days / 30) + "M";
            } else if (days >= 365) {
                return (int) (days / 365) + "y";
            }

        } catch (java.text.ParseException e) {
            //
        }
        return null;
    }
}