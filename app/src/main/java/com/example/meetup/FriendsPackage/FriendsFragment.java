package com.example.meetup.FriendsPackage;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase, mUsers;
    private FirebaseRecyclerOptions<Friends> foptions;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> fadapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = mView.findViewById(R.id.friends_list);
        mFriendsList.setHasFixedSize(true);

        String current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user);
        mFriendsDatabase.keepSynced(true);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

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
                                Picasso.with(getContext()).load(img_datasnap).networkPolicy(NetworkPolicy.OFFLINE).into(friendsViewHolder.userImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext()).load(img_datasnap).into(friendsViewHolder.userImage);
                                    }
                                });
                            } else {
                                Picasso.with(getContext()).load(R.drawable.default_img).into(friendsViewHolder.userImage);
                            }
                        }

                        friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);
//                        Toasty.info(getApplicationContext(), usersViewHolder.userName.getText(), Toast.LENGTH_SHORT, true).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStop() {
        super.onStop();
        fadapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        fadapter.startListening();
    }
}
