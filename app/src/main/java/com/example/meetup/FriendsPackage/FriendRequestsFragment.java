package com.example.meetup.FriendsPackage;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Picasso;
import com.theophrast.ui.widget.SquareImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestsFragment extends Fragment {

    private RecyclerView mFriendReqList;
    private DatabaseReference mFriendReqDatabase, mUsers;
    private FirebaseRecyclerOptions<Friends> froptions;
    private SquareImageView frImage;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> fradapter;


    public FriendRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        mFriendReqList = mView.findViewById(R.id.friend_req_list);
        mFriendReqList.setHasFixedSize(true);

        String current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(current_user);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        froptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendReqDatabase, Friends.class)
                .build();

        fradapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(froptions) {
            @Override
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int i, final Friends friends) {
                final String user_id = getRef(i).getKey();
                String req_type = friends.getRequest_type();

                if (req_type.equals("sent")){
                    friendsViewHolder.userStatus.setText("Request sent");
                } else {
                    friendsViewHolder.userStatus.setText("Request received");
                }

                mUsers.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String img_datasnap = dataSnapshot.child("img_thumbnail").getValue().toString();
                            String name_datasnap = dataSnapshot.child("name").getValue().toString();

                            friendsViewHolder.userName.setText(name_datasnap);

                            if (img_datasnap.equals("default")){
                                Picasso.with(getContext()).load(R.drawable.default_img).into(friendsViewHolder.userImage);
                            } else {
                                Picasso.with(getContext()).load(img_datasnap).placeholder(R.drawable.default_img).into(friendsViewHolder.userImage);
                            }
                            friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }
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

        mFriendReqList.setAdapter(fradapter);
        fradapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        fradapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        fradapter.startListening();
    }

}
