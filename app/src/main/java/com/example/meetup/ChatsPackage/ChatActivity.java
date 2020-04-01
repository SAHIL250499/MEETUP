package com.example.meetup.ChatsPackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.meetup.MessagesPackage.MessageAdapter;
import com.example.meetup.MessagesPackage.Messages;
import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.example.meetup.UsersPackage.UsersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.meetup.SubstituteCipher;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mChatToolBar;
    private TextView mDisplayName, mDisplayLastSeen;
    private CircleImageView mDisplayProfile;
    private ImageView onlineDot;
    private RecyclerView mMessagesList;

    private ImageButton chatAddBtn, chatSendBtn;
    private EditText chatTypeMsg;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private LinearLayoutManager linearLayoutManager;

    private List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
        Date date = new Date();
        final String CURRENT_DATE = formatter.format(date);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        final String current_user = mAuth.getCurrentUser().getUid();
        final String uid = getIntent().getStringExtra("user_id");
        final String user_name = getIntent().getStringExtra("user_name");

        mChatToolBar = findViewById(R.id.chat_appBar);
        setSupportActionBar(mChatToolBar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_layout, null);

        actionBar.setCustomView(action_bar_view);

        mDisplayName = findViewById(R.id.custom_bar_title);
        mDisplayLastSeen = findViewById(R.id.custom_bar_seen);
        mDisplayProfile = findViewById(R.id.custom_bar_image);

        messageAdapter = new MessageAdapter(messagesList);

        mMessagesList = findViewById(R.id.messages_list);
        linearLayoutManager = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayoutManager);

        mMessagesList.setAdapter(messageAdapter);

        chatAddBtn = findViewById(R.id.chat_add_btn);
        chatTypeMsg = findViewById(R.id.chat_message_view);
        chatSendBtn = findViewById(R.id.chat_send_btn);

        onlineDot = findViewById(R.id.online_dot);

        mRootRef.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String user_profile_image = dataSnapshot.child("img_thumbnail").getValue().toString();
                    String online_status = dataSnapshot.child("online_at").getValue().toString();


                    String days = getDaysBtwDates(online_status, CURRENT_DATE);

                    mDisplayName.setText(user_name);

                    if (days.equals("1m")) {
                        mDisplayLastSeen.setText("Active now");
                        onlineDot.setVisibility(View.VISIBLE);

                    } else {
                        mDisplayLastSeen.setText(days);
                        onlineDot.setVisibility(View.INVISIBLE);
                    }

                    Picasso.with(getApplicationContext()).load(user_profile_image).into(mDisplayProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chats").child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(uid)) {

                    mRootRef.child("Notifications/Messages/" + current_user).removeValue();
                    mRootRef.child("Notifications/Messages/" + uid).removeValue();

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + current_user + "/" + uid, chatAddMap);
                    chatUserMap.put("Chat/" + uid + "/" + current_user, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadMessages(current_user, uid);

        linearLayoutManager.scrollToPosition(messagesList.size() - 1);

        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = chatTypeMsg.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    chatTypeMsg.setText("");
                    String current_user_ref = "Messages/" + current_user + "/" + uid;
                    String chat_user_ref = "Messages/" + uid + "/" + current_user;

                    DatabaseReference user_message_push = mRootRef.child("Messages")
                            .child(current_user).child(uid).push();
                    String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    final SubstituteCipher cipher = new SubstituteCipher();

                    messageMap.put("message", cipher.encode(message, current_user));
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("date", CURRENT_DATE);
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", current_user);
                    messageMap.put("encrypted", true);
                    messageMap.put("message_id", push_id);
                    messageMap.put("to", uid);
                    messageMap.put("deleted", false);

                    Map messageUsermap = new HashMap();
                    messageUsermap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUsermap.put(chat_user_ref + "/" + push_id, messageMap);

                    mRootRef.updateChildren(messageUsermap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                linearLayoutManager.scrollToPosition(messagesList.size() - 1);

                                Map notifMap = new HashMap();
                                notifMap.put("from", current_user);
                                notifMap.put("msg", cipher.encode(message, current_user));
                                mRootRef.child("Notifications").child("Messages").child(uid).push().setValue(notifMap);
                            } else {
                                chatTypeMsg.setText(message);
                            }
                        }
                    });
                }
            }
        });

    }

    private void loadMessages(final String current_user, final String uid) {
        DatabaseReference messageRef = mRootRef.child("Messages").child(current_user).child(uid);
        Query query = messageRef.limitToLast(100);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();
                linearLayoutManager.scrollToPosition(messagesList.size() - 1);
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
