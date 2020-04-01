package com.example.meetup.ProfilePackage;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.DetailsActivity;
import com.example.meetup.FriendsPackage.FriendsListActivity;
import com.example.meetup.NewsFeed.CommentActivity;
import com.example.meetup.PostsPackage.PostDetailActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private TextView userName, userStatus, noOfPosts, noOfFriends, lastSeen, userUserName;
    private MaterialButton editProfileBtn;
    private RecyclerView profileThumbsList;

    private LinearLayout friendsLayout;

    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mPostsDatabase, mFriendsDatabase;

    private FirebaseRecyclerAdapter<PostThumb, PostThumbViewHolder> adapter;
    private FirebaseRecyclerOptions<PostThumb> options;

    private String current_user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_profile, container, false);

        userName = mView.findViewById(R.id.frag_profile_name);
        userStatus = mView.findViewById(R.id.frag_profile_status);
        noOfPosts = mView.findViewById(R.id.no_of_posts);
        noOfFriends = mView.findViewById(R.id.no_of_friends);
        userUserName = mView.findViewById(R.id.frag_user_name);
        lastSeen = mView.findViewById(R.id.last_seen);

        friendsLayout = mView.findViewById(R.id.friends_lay);

        profileImage = mView.findViewById(R.id.frag_user_image);

        editProfileBtn = mView.findViewById(R.id.frag_edit_profile);

        profileThumbsList = mView.findViewById(R.id.frag_thumb_list);

        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users/" + current_user);
        mPostsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts/" + current_user);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends/" + current_user);

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), DetailsActivity.class));
            }
        });

        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FriendsListActivity.class);
                i.putExtra("friends_uid", current_user);
                startActivity(i);
            }
        });

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String user_name = dataSnapshot.child("username").getValue().toString();
                    final String userImage = dataSnapshot.child("img_thumbnail").getValue().toString();
                    String online_date = dataSnapshot.child("online_at").getValue().toString();
                    userName.setText(name);
                    userUserName.setText(user_name);
                    userStatus.setText(status);

                    if (!userImage.equals("default")) {
                        Picasso.with(getContext()).load(userImage)
                                .placeholder(R.drawable.default_img)
                                .into(profileImage);
                    } else {
                        Picasso.with(getContext())
                                .load(R.drawable.default_img)
                                .into(profileImage);
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
                    Date date = new Date();
                    String CURRENT_DATE = formatter.format(date);

                    String online_status = getDaysBtwDates(online_date, CURRENT_DATE);

                    if (online_status.equals("1m")){
                        lastSeen.setText("now");
                        lastSeen.setTextColor(Color.parseColor("#0bff07"));
                    } else {
                        lastSeen.setText(online_status);
                        lastSeen.setTextColor(getResources().getColor(R.color.colorBlack));
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String friends_count = String.valueOf(dataSnapshot.getChildrenCount());
                noOfFriends.setText(friends_count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPostsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String posts_count = String.valueOf(dataSnapshot.getChildrenCount());
                noOfPosts.setText(posts_count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<PostThumb>()
                .setQuery(mPostsDatabase, PostThumb.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<PostThumb, PostThumbViewHolder>(options) {

            @Override
            public PostThumb getItem(int position) {
                return super.getItem(getItemCount() - 1 - position);
            }

            @Override
            protected void onBindViewHolder(final PostThumbViewHolder postThumbViewHolder, int i, final PostThumb postThumb) {

                Picasso.with(getContext()).load(postThumb.getPost_image_thumbnail())
                        .into(postThumbViewHolder.postThumbView);

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
                View mView = LayoutInflater.from(getContext())
                        .inflate(R.layout.thumb_post_single_layout, parent, false);

                return new PostThumbViewHolder(mView);
            }
        };
        profileThumbsList.setAdapter(adapter);
        adapter.startListening();
    }

    private String getDaysBtwDates(String date1, String date2){
        long days;

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");

        try{
            Date dt1 = myFormat.parse(date1);
            Date dt2 = myFormat.parse(date2);

            long diff = dt2.getTime() - dt1.getTime();
            days = diff / 1000L / 60L / 60L / 24L;

            if (days == 0){
                long diff_m = (dt2.getTime() - dt1.getTime()) / 1000 / 60;

                if (diff_m == 0){
                    return "1m";
                }
                else if (diff_m >= 60){
                    return (int)(diff_m / 60) + "h";
                }
                else {
                    return diff_m + "m";
                }
            }
            else if(days > 0 && days < 7){
                return (int)(days) + "d";
            }
            else if (days >= 7 && days <= 29){
                return (int)(days / 7) + "w";
            }
            else  if (days >= 30 && days <= 364){
                return (int)(days / 30) + "M";
            }
            else if(days >= 365) {
                return (int)(days / 365) + "y";
            }

        } catch (java.text.ParseException e){
            //
        }
        return null;
    }
}
