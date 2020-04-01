package com.example.meetup.UsersPackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> adapter;
    private FirebaseRecyclerOptions<Users> options;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_appBar);
        mUsersList = findViewById(R.id.users_list);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mUsersDatabase, Users.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final UsersViewHolder usersViewHolder, int i, final Users users) {
                if (usersViewHolder != null && users != null){
                    // Load Display Name
                    usersViewHolder.userName.setText(users.getUsername());

                    // Load Status if characters exceed 133 then truncate it to 133
                    if (users.getStatus().length() > 133)
                        usersViewHolder.userStatus.setText(users.getStatus().substring(0, 133) + "...");
                    else
                        usersViewHolder.userStatus.setText(users.getStatus());

                    // Load Image With Placeholder
                    if (!users.getImg_thumbnail().equals("default"))
                        Picasso.with(UsersActivity.this).load(users.getImg_thumbnail()).placeholder(R.drawable.default_img).into(usersViewHolder.profileImage);

                    final String user_id = getRef(i).getKey();

                    usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", user_id);
                            profileIntent.putExtra("user_name", users.getName());
                            startActivity(profileIntent);
//                        Toasty.info(getApplicationContext(), usersViewHolder.userName.getText(), Toast.LENGTH_SHORT, true).show();
                        }
                    });
                }

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(mView);
            }
        };
        mUsersList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        adapter.startListening();
    }
}