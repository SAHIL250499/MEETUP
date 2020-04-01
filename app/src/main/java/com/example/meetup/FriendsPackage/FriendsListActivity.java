package com.example.meetup.FriendsPackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase, mUsers;
    private FirebaseRecyclerOptions<Friends> foptions;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> fadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mFriendsList = findViewById(R.id.act_friends_list);
        mFriendsList.setHasFixedSize(true);

        String user = getIntent().getStringExtra("friends_uid");

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(user);
        mFriendsDatabase.keepSynced(true);

        foptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsDatabase.orderByChild("date"), Friends.class)
                .build();

        fadapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(foptions) {
            @Override
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int i, final Friends friends) {
                final String user_id = getRef(i).getKey();
                mUsers.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            final String img_datasnap = dataSnapshot.child("img_thumbnail").getValue().toString();
                            String name_datasnap = dataSnapshot.child("username").getValue().toString();
                            String status_datasnap = dataSnapshot.child("status").getValue().toString();

                            if (status_datasnap.length() > 133){
                                friendsViewHolder.userStatus.setText(status_datasnap.substring(0, 133) + "...");
                            } else{
                                friendsViewHolder.userStatus.setText(status_datasnap);
                            }

                            friendsViewHolder.userName.setText(name_datasnap);

                            if (!img_datasnap.equals("default")){
                                Picasso.with(getApplicationContext()).load(img_datasnap).networkPolicy(NetworkPolicy.OFFLINE).into(friendsViewHolder.userImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getApplicationContext()).load(img_datasnap).into(friendsViewHolder.userImage);
                                    }
                                });
                            } else {
                                Picasso.with(getApplicationContext()).load(R.drawable.default_img).into(friendsViewHolder.userImage);
                            }
                        }

                        friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);
//                        Toasty.info(getApplicationContext(), usersViewHolder.userName.getText(), Toast.LENGTH_SHORT, true).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(mView);
            }
        };

        mFriendsList.setAdapter(fadapter);
        fadapter.startListening();
    }
}
