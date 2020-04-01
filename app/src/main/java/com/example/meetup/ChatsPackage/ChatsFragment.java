package com.example.meetup.ChatsPackage;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetup.FriendsPackage.Friends;
import com.example.meetup.FriendsPackage.FriendsViewHolder;
import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mChatsList;
    private DatabaseReference mChatsDatabase, mUsers, mMessagesDatabase;
    private FirebaseRecyclerOptions<Chats> options;
    private FirebaseRecyclerAdapter<Chats, ChatsViewHolder> adapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsList = mView.findViewById(R.id.chats_list);
        mChatsList.setHasFixedSize(true);

        String current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user);
        mMessagesDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(current_user);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<Chats>()
                .setQuery(mChatsDatabase, Chats.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final ChatsViewHolder chatsViewHolder, int i, final Chats chats) {
                final String user_id = getRef(i).getKey();
                Log.d("USER", user_id);
                mUsers.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String user__id = dataSnapshot.child("user_id").getValue().toString();
                            String img_datasnap = dataSnapshot.child("img_thumbnail").getValue().toString();
                            final String name_datasnap = dataSnapshot.child("name").getValue().toString();
                            String status_datasnap = dataSnapshot.child("status").getValue().toString();

                            chatsViewHolder.chatUserName.setText(name_datasnap);

                            if (img_datasnap.equals("default")) {
                                Picasso.with(getContext()).load(R.drawable.default_img).into(chatsViewHolder.chatUserProfileImage);
                            } else {
                                Picasso.with(getContext()).load(img_datasnap).placeholder(R.drawable.default_img).into(chatsViewHolder.chatUserProfileImage);
                            }

                            chatsViewHolder.chatUserStatus.setText(status_datasnap);

                            chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", user_id);
                                    chatIntent.putExtra("user_name", name_datasnap);
                                    startActivity(chatIntent);
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
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new ChatsViewHolder(mView);
            }
        };
        mChatsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }
}
