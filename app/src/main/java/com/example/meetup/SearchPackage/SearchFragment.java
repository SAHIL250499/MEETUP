package com.example.meetup.SearchPackage;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.example.meetup.UsersPackage.UsersActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private EditText searchText;
    private ImageButton searchBtn;
    private RelativeLayout relativeLayout;
    private ConstraintLayout constraintLayout;
    private TextView userName, userStatus;
    private CircleImageView profileImage;

    private String user_id;

    private DatabaseReference userNamesDatabase, usersDatabase, rootRef;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBtn = view.findViewById(R.id.search_btn);
        searchText = view.findViewById(R.id.search_field);

        relativeLayout = view.findViewById(R.id.relativeLayout);
        constraintLayout = view.findViewById(R.id.search_users_layout);
        userName = relativeLayout.findViewById(R.id.posts_single_username);
        userStatus = relativeLayout.findViewById(R.id.posts_single_desc);
        profileImage = relativeLayout.findViewById(R.id.posts_user_single_profile_image);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userNamesDatabase = rootRef.child("Usernames");
        usersDatabase = rootRef.child("Users");

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String search_text = searchText.getText().toString().trim().toLowerCase();
                if (!TextUtils.isEmpty(search_text)){
                    if (validateUsername(search_text)){
                        userNamesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(search_text)){
                                    user_id = dataSnapshot.child(search_text).child("user_id").getValue().toString();

                                    usersDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                String username = dataSnapshot.child("username").getValue().toString();
                                                String status = dataSnapshot.child("status").getValue().toString();
                                                String profileUrl = dataSnapshot.child("img_thumbnail").getValue().toString();

                                                userName.setText(username);
                                                userStatus.setText(status);
                                                Picasso.with(getContext()).load(profileUrl).into(profileImage);
                                                constraintLayout.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                    constraintLayout.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                profileIntent.putExtra("user_id", user_id);
                startActivity(profileIntent);
            }
        });

    }

    private boolean validateUsername(String new_username) {
        return (Pattern.matches("^[a-z0-9_]+$", new_username) && new_username.length() > 3 && new_username.length() < 19);
    }
}
