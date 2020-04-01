package com.example.meetup.NewsFeed;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.PostsPackage.LikesListActivity;
import com.example.meetup.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theophrast.ui.widget.SquareImageView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentsViewHolder> {

    private List<Comment> mCommentsList;

    public CommentAdapter(List<Comment> mCommentsList) {
        this.mCommentsList = mCommentsList;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_comment_layout, parent, false);
        return new CommentsViewHolder(v);

    }

    class CommentsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView commentProfileImage;
        TextView userName, userComment, datePosted, likeBtn, noOfLikes, moreBtn, unlikeBtn;

        CommentsViewHolder(View view) {
            super(view);
            commentProfileImage = view.findViewById(R.id.single_comment_profile);
            userName = view.findViewById(R.id.single_comment_name);
            userComment = view.findViewById(R.id.single_comment);
            datePosted = view.findViewById(R.id.comment_date_posted);
            noOfLikes = view.findViewById(R.id.single_comment_likes);
            likeBtn = view.findViewById(R.id.single_comment_like_btn);
            unlikeBtn = view.findViewById(R.id.single_comment_unlike_btn);
            moreBtn = view.findViewById(R.id.single_comment_more_btn);
        }
    }

    @Override
    public void onBindViewHolder(final CommentsViewHolder viewHolder, final int i) {

        Collections.sort(mCommentsList, new Comparator<Comment>() {
            @Override
            public int compare(Comment c1, Comment c2) {
                Long param1 = Long.parseLong(c1.getTimestamp());
                Long param2 = Long.parseLong(c2.getTimestamp());
                Long param = param1 - param2;
                return Math.toIntExact(param);
            }
        });

        final DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference();

        final Comment comment = mCommentsList.get(i);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference comment_likesDB = FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child(comment.getParent_post_id())
                .child(comment.getPost_id())
                .child("comments")
                .child(comment.getTimestamp())
                .child("likes");


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
        Date date = new Date();
        String CURRENT_DATE = formatter.format(date);

        String getTimeAgo = getDaysBtwDates(comment.getDate(), CURRENT_DATE);

        viewHolder.datePosted.setText(getTimeAgo);

        viewHolder.userComment.setText(comment.getComment());
        viewHolder.userName.setText(comment.getUsername());

        Picasso.with(viewHolder.commentProfileImage.getContext())
                .load(comment.getProfile_image())
                .into(viewHolder.commentProfileImage);

        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment_likesDB.child(uid).child("user_id").setValue(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        viewHolder.likeBtn.setVisibility(View.GONE);
                        viewHolder.unlikeBtn.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        viewHolder.unlikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment_likesDB.child(uid).child("user_id").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        viewHolder.likeBtn.setVisibility(View.VISIBLE);
                        viewHolder.unlikeBtn.setVisibility(View.GONE);
                    }
                });
            }
        });

        viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (uid.equals(comment.getFrom())) {
                    final PopupMenu menu = new PopupMenu(viewHolder.moreBtn.getContext(), viewHolder.moreBtn);
                    menu.inflate(R.menu.comment_menu);
                    menu.show();
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.comment_del:
                                    String parent_post_id = comment.getParent_post_id();
                                    String post_id = comment.getPost_id();
                                    String comment_id = comment.getTimestamp();
                                    commentRef.child("Posts")
                                            .child(parent_post_id)
                                            .child(post_id)
                                            .child("comments")
                                            .child(comment_id)
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    viewHolder.itemView.setVisibility(View.GONE);
                                                }
                                            });
                                    viewHolder.itemView.setVisibility(View.GONE);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                }
            }
        });

        viewHolder.noOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user_id = comment.getParent_post_id();
                String post_id = comment.getPost_id();
                String comment_id = comment.getTimestamp();

                Intent comment_likes_i = new Intent(viewHolder.noOfLikes.getContext(), LikesListActivity.class);
                comment_likes_i.putExtra("user_id", user_id);
                comment_likes_i.putExtra("post_id", post_id);
                comment_likes_i.putExtra("comment_id", comment_id);
                comment_likes_i.putExtra("comment_flag", "true");
                viewHolder.noOfLikes.getContext().startActivity(comment_likes_i);
            }
        });

        comment_likesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int likes = (int) dataSnapshot.getChildrenCount();
                if (dataSnapshot.hasChild(uid)) {
                    viewHolder.likeBtn.setVisibility(View.GONE);
                    viewHolder.unlikeBtn.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.unlikeBtn.setVisibility(View.GONE);
                    viewHolder.likeBtn.setVisibility(View.VISIBLE);
                }
                if (likes == 1) {
                    viewHolder.noOfLikes.setText(likes + " Like");
                } else {
                    viewHolder.noOfLikes.setText(likes + " Likes");
                }
                if (likes > 0) {
                    viewHolder.noOfLikes.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.noOfLikes.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentsList.size();
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

