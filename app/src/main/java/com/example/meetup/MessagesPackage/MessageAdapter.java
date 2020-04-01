package com.example.meetup.MessagesPackage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.meetup.SubstituteCipher;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageReceiverText, messageSenderText, msgSenderTime, msgReceiverTime;

        public MessageViewHolder(View view) {
            super(view);
            messageReceiverText = view.findViewById(R.id.message_receiver_text);
            messageSenderText = view.findViewById(R.id.message_sender_text);
            msgReceiverTime = view.findViewById(R.id.receiver_msg_time);
            msgSenderTime = view.findViewById(R.id.sender_msg_time);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        final SubstituteCipher cipher = new SubstituteCipher();
        final String current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference mMessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        final Messages c = mMessageList.get(i);
        final String from_user = c.getFrom();

        String smsgTime = getMsgTime(c.getDate());

        if (from_user.equals(current_user)) {

            if (c.isDeleted()) {
                viewHolder.messageSenderText.setText("This message was deleted");
                viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_deleted);
                viewHolder.messageSenderText.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                viewHolder.messageSenderText.setBackgroundResource(R.drawable.message_from_background);
                viewHolder.messageSenderText.setTextColor(Color.parseColor("#ffffff"));
                viewHolder.messageSenderText.setText(c.getMessage());
            }
            viewHolder.messageSenderText.setVisibility(View.VISIBLE);
            viewHolder.messageReceiverText.setVisibility(View.GONE);
            viewHolder.msgSenderTime.setText(smsgTime);
            viewHolder.msgReceiverTime.setVisibility(View.GONE);

            viewHolder.messageSenderText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String new_text = viewHolder.messageSenderText.getText().toString();
                    if (!c.isDeleted()) {
                        if (c.isEncrypted()) {
                            new_text = cipher.decode(new_text, c.getFrom());
                            viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_sent_decrypted);
                            c.setEncrypted(false);
                        } else {
                            new_text = cipher.encode(new_text, c.getFrom());
                            viewHolder.messageSenderText.setBackgroundResource(R.drawable.message_from_background);
                            c.setEncrypted(true);
                        }

                        viewHolder.messageSenderText.setText(new_text.replaceAll("\\s+", " "));
                    } else {
                        viewHolder.messageSenderText.setText("This message was deleted");
                        viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_deleted);
                        viewHolder.messageSenderText.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                }
            });

//            viewHolder.messageSenderText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String msg = viewHolder.messageSenderText.getText().toString();
//                    Map messageMap = new HashMap();
//                    if (!c.isDeleted()) {
//                        if (c.isEncrypted()) {
//                            String dec_msg = cipher.decode(msg, c.getFrom());
//                            messageMap.put("encrypted", false);
//                            messageMap.put("message", dec_msg);
//                            viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_sent_decrypted);
//                            viewHolder.messageSenderText.setText(dec_msg.replaceAll("\\s+", " "));
//                            c.setEncrypted(false);
//                        } else {
//                            String enc_msg = cipher.encode(msg, c.getFrom());
//                            messageMap.put("encrypted", true);
//                            messageMap.put("message", enc_msg);
//                            viewHolder.messageSenderText.setBackgroundResource(R.drawable.message_from_background);
//                            viewHolder.messageSenderText.setText(enc_msg.replaceAll("\\s+", " "));
//                            c.setEncrypted(true);
//                        }
//                        mMessagesRef.child(c.getFrom()).child(c.getTo()).child(c.getMessage_id()).updateChildren(messageMap);
//                        mMessagesRef.child(c.getTo()).child(c.getFrom()).child(c.getMessage_id()).updateChildren(messageMap);
//                    } else {
//                        viewHolder.messageSenderText.setText("This message was deleted");
//                        viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_deleted);
//                        viewHolder.messageSenderText.setTextColor(Color.parseColor("#FFFFFF"));
//                    }
//                }
//            });

            viewHolder.messageSenderText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    PopupMenu popupMenu = new PopupMenu(viewHolder.messageSenderText.getContext(), viewHolder.messageSenderText);
                    popupMenu.inflate(R.menu.message_sender_menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {

                                case R.id.del_message:
                                    mMessagesRef.child(c.getFrom()).child(c.getTo()).child(c.getMessage_id()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    viewHolder.messageSenderText.setVisibility(View.GONE);
                                                    viewHolder.msgSenderTime.setVisibility(View.GONE);
                                                }
                                            });
                                    return true;

                                case R.id.del_everyone:
                                    Map messageMap = new HashMap();

                                    messageMap.put("encrypted", false);
                                    messageMap.put("message", "This message was deleted");
                                    messageMap.put("deleted", true);

                                    mMessagesRef.child(c.getFrom()).child(c.getTo()).child(c.getMessage_id()).updateChildren(messageMap);
                                    mMessagesRef.child(c.getTo()).child(c.getFrom()).child(c.getMessage_id()).updateChildren(messageMap);

                                    viewHolder.messageSenderText.setText("This message was deleted");
                                    viewHolder.messageSenderText.setBackgroundResource(R.drawable.msg_deleted);
                                    viewHolder.messageSenderText.setTextColor(Color.parseColor("#FFFFFF"));

                                    viewHolder.msgSenderTime.setVisibility(View.VISIBLE);

                                    c.setDeleted(true);
                                    return true;

                                case R.id.copy_msg:
                                    final android.content.ClipboardManager clipboardManager = (ClipboardManager) viewHolder.itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
                                    ClipData data = ClipData.newPlainText("Source Text", viewHolder.messageSenderText.getText().toString());
                                    clipboardManager.setPrimaryClip(data);
                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            });


        } else {
            String rmsgTime = getMsgTime(c.getDate());
            if (c.isDeleted()) {
                viewHolder.messageReceiverText.setText("This message was deleted");
                viewHolder.messageReceiverText.setBackgroundResource(R.drawable.msg_recv_deleted);
                viewHolder.messageReceiverText.setTextColor(Color.parseColor("#000000"));
            } else {
                viewHolder.messageReceiverText.setBackgroundResource(R.drawable.message_text_background);
                viewHolder.messageReceiverText.setTextColor(Color.parseColor("#000000"));
                viewHolder.messageReceiverText.setText(c.getMessage());
            }
            viewHolder.messageReceiverText.setVisibility(View.VISIBLE);
            viewHolder.messageSenderText.setVisibility(View.GONE);
            viewHolder.msgReceiverTime.setText(rmsgTime);
            viewHolder.msgSenderTime.setVisibility(View.GONE);

            viewHolder.messageReceiverText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String new_text = viewHolder.messageReceiverText.getText().toString();
                    if (!c.isDeleted()) {
                        if (c.isEncrypted()) {
                            new_text = cipher.decode(new_text, c.getFrom());
                            c.setEncrypted(false);
                            viewHolder.messageReceiverText.setBackgroundResource(R.drawable.msg_recv_decrypted);
                        } else {
                            new_text = cipher.encode(new_text, c.getFrom());
                            c.setEncrypted(true);
                            viewHolder.messageReceiverText.setBackgroundResource(R.drawable.message_text_background);
                        }
                        viewHolder.messageReceiverText.setText(new_text.replaceAll("\\s+", " "));
                    } else {
                        viewHolder.messageReceiverText.setText("This message was deleted");
                        viewHolder.messageReceiverText.setBackgroundResource(R.drawable.msg_recv_deleted);
                        viewHolder.messageReceiverText.setTextColor(Color.parseColor("#000000"));
                    }
                }
            });

//            viewHolder.messageReceiverText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String msg = viewHolder.messageReceiverText.getText().toString();
//                    Map messageMap = new HashMap();
//                    if (!c.isDeleted()) {
//                        if (c.isEncrypted()) {
//                            String dec_msg = cipher.decode(msg, c.getFrom());
//                            messageMap.put("encrypted", false);
//                            messageMap.put("message", dec_msg);
//                            viewHolder.messageReceiverText.setBackgroundResource(R.drawable.msg_recv_decrypted);
//                            viewHolder.messageReceiverText.setText(dec_msg.replaceAll("\\s+", " "));
//                            c.setEncrypted(false);
//                        } else {
//                            String enc_msg = cipher.encode(msg, c.getFrom());
//                            messageMap.put("encrypted", true);
//                            messageMap.put("message", enc_msg);
//                            viewHolder.messageReceiverText.setBackgroundResource(R.drawable.message_text_background);
//                            viewHolder.messageReceiverText.setText(enc_msg.replaceAll("\\s+", " "));
//                            c.setEncrypted(true);
//                        }
//                        mMessagesRef.child(c.getFrom()).child(c.getTo()).child(c.getMessage_id()).updateChildren(messageMap);
//                        mMessagesRef.child(c.getTo()).child(c.getFrom()).child(c.getMessage_id()).updateChildren(messageMap);
//                    } else {
//                        viewHolder.messageReceiverText.setText("This message was deleted");
//                        viewHolder.messageReceiverText.setBackgroundResource(R.drawable.msg_recv_deleted);
//                        viewHolder.messageReceiverText.setTextColor(Color.parseColor("#000000"));
//                    }
//                }
//            });

            viewHolder.messageReceiverText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (!from_user.equals(current_user)){
                        PopupMenu popupMenu = new PopupMenu(viewHolder.messageReceiverText.getContext(), viewHolder.messageReceiverText);
                        popupMenu.inflate(R.menu.message_receiver_menu);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {

                                    case R.id.del_message:
                                        mMessagesRef.child(c.getTo()).child(c.getFrom()).child(c.getMessage_id()).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        viewHolder.messageReceiverText.setVisibility(View.GONE);
                                                        viewHolder.msgReceiverTime.setVisibility(View.GONE);
                                                    }
                                                });
                                        return true;

                                    case R.id.copy_msg:
                                        final android.content.ClipboardManager clipboardManager = (ClipboardManager) viewHolder.itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
                                        ClipData data = ClipData.newPlainText("Source Text", viewHolder.messageReceiverText.getText().toString());
                                        clipboardManager.setPrimaryClip(data);
                                        return true;

                                    default:
                                        return false;
                                }
                            }
                        });
                        popupMenu.show();
                        return true;
                    }


                    Toast.makeText(viewHolder.messageReceiverText.getContext(), "This was long pressed", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    private String getMsgTime(String date) {
        String[] date_part = date.split("-")[1].split(":");
        return date_part[0] + ":" + date_part[1];
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}

