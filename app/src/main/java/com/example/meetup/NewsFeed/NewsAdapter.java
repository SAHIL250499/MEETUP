package com.example.meetup.NewsFeed;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.FriendsPackage.FriendsListActivity;
import com.example.meetup.PostsPackage.LikesListActivity;
import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theophrast.ui.widget.SquareImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> mNewsList;

    public NewsAdapter(List<News> mNewsList) {
        this.mNewsList = mNewsList;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.posts_single_layout, parent, false);
        return new NewsViewHolder(v);
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView image_card_view, text_card_view;
        CircleImageView profileImageView;
        SquareImageView postImageView;
        TextView userNameText, userDescText, userDatePostedText, postOptions, likedBy;
        ProgressBar loadingBar;
        MaterialButton likeBtn, unlikeBtn, commentBtn;

        NewsViewHolder(View view) {
            super(view);
            profileImageView = view.findViewById(R.id.posts_user_single_profile_image);
            postImageView = view.findViewById(R.id.posts_single_image);
            userNameText = view.findViewById(R.id.posts_single_username);
            userDescText = view.findViewById(R.id.posts_single_desc);
            userDatePostedText = view.findViewById(R.id.posts_date_posted);
            loadingBar = view.findViewById(R.id.loading_bar);
            postOptions = view.findViewById(R.id.post_options);
            likeBtn = view.findViewById(R.id.post_like_btn);
            unlikeBtn = view.findViewById(R.id.post_unlike_btn);
            likedBy = view.findViewById(R.id.liked_by);
            commentBtn = view.findViewById(R.id.post_comment_btn);
            image_card_view = view.findViewById(R.id.card);
            text_card_view = view.findViewById(R.id.text_post_card);
        }
    }

    @Override
    public void onBindViewHolder(final NewsViewHolder viewHolder, int i) {

        Collections.sort(mNewsList, new Comparator<News>() {
            @Override
            public int compare(News n1, News n2) {
                Long param1 = Long.parseLong(n1.getTimestamp());
                Long param2 = Long.parseLong(n2.getTimestamp());
                Long param = param2 - param1;
                return Math.toIntExact(param);
            }
        });

        final News news = mNewsList.get(i);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference mPostDatabase = FirebaseDatabase.getInstance().getReference().child("Posts/" + news.getPosted_by());

        viewHolder.userNameText.setText(news.getName());

        if (!news.getPost_desc().equals("")) {
            viewHolder.userDescText.setText(news.getPost_desc());
            viewHolder.userDescText.setVisibility(View.VISIBLE);
        } else {
            viewHolder.userDescText.setVisibility(View.GONE);
        }

        if (!news.getPosted_by().equals(uid)){
            viewHolder.postOptions.setVisibility(View.GONE);
        } else {
            viewHolder.postOptions.setVisibility(View.VISIBLE);
        }

        if (news.getPost_type().equals("image_story")){
            Context context = viewHolder.itemView.getContext();
            viewHolder.image_card_view.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorStoriesBG)));
            viewHolder.userNameText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
            viewHolder.userDescText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
            viewHolder.postOptions.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
            viewHolder.userDatePostedText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
            viewHolder.likedBy.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));

            viewHolder.commentBtn.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorWarning)));
            viewHolder.commentBtn.setStrokeColorResource(R.color.colorWarning);
            viewHolder.commentBtn.setRippleColorResource(R.color.colorWarning);

            Date date = new Date();
            SimpleDateFormat t_formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String NOW_TIMESTAMP = t_formatter.format(date);

            if (Long.parseLong(NOW_TIMESTAMP) > getNextDate(news.getTimestamp())){
                // Post Expired - Delete Post
                if (news.getPosted_by().equals(uid)){
                    mPostDatabase.child(news.getTimestamp()).removeValue();
                }
            }

        } else {
            Context context = viewHolder.itemView.getContext();
            viewHolder.image_card_view.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
            viewHolder.userNameText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorDark)));
            viewHolder.userDescText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorDark)));
            viewHolder.postOptions.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorDark)));
            viewHolder.userDatePostedText.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorDark)));
            viewHolder.likedBy.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorDark)));

            viewHolder.commentBtn.setTextColor(ColorStateList.valueOf(context.getColor(R.color.colorInfo)));
            viewHolder.commentBtn.setStrokeColorResource(R.color.colorInfo);
            viewHolder.commentBtn.setRippleColorResource(R.color.colorInfo);
        }

        viewHolder.postOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (news.getPosted_by().equals(uid)) {
                    PopupMenu popupMenu = new PopupMenu(viewHolder.postOptions.getContext(), viewHolder.postOptions);
                    popupMenu.inflate(R.menu.posts_menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.del_post:
                                    mPostDatabase.child(news.getTimestamp()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(viewHolder.postOptions.getContext(), "Post Deleted!", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(viewHolder.postOptions.getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            }
        });

        viewHolder.userNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(viewHolder.userNameText.getContext(), ProfileActivity.class);
                i.putExtra("user_id", news.getPosted_by());
                viewHolder.userNameText.getContext().startActivity(i);
            }
        });

        viewHolder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(viewHolder.profileImageView.getContext(), ProfileActivity.class);
                i.putExtra("user_id", news.getPosted_by());
                viewHolder.profileImageView.getContext().startActivity(i);
            }
        });

        viewHolder.likedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(viewHolder.likedBy.getContext(), LikesListActivity.class);
                i.putExtra("user_id", news.getPosted_by());
                i.putExtra("post_id", news.getTimestamp());
                i.putExtra("comment_flag", "false");
                viewHolder.likedBy.getContext().startActivity(i);
            }
        });

        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(viewHolder.commentBtn.getContext(), CommentActivity.class);
                i.putExtra("user_id", news.getPosted_by());
                i.putExtra("post_id", news.getTimestamp());
                viewHolder.commentBtn.getContext().startActivity(i);
            }
        });

        mPostDatabase.child(news.getTimestamp() + "/likes").addValueEventListener(new ValueEventListener() {
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
                if (likes == 1){
                    viewHolder.likedBy.setText(likes + " Like");
                } else {
                    viewHolder.likedBy.setText(likes + " Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPostDatabase.child(news.getTimestamp() + "/comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int comments = (int) dataSnapshot.getChildrenCount();
                if (comments == 1){
                    viewHolder.commentBtn.setText(comments + " Comment");
                } else {
                    viewHolder.commentBtn.setText(comments + " Comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        viewHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostDatabase.child(news.getTimestamp() + "/likes/" + uid + "/user_id").setValue(uid)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
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
                mPostDatabase.child(news.getTimestamp() + "/likes/" + uid + "/user_id").removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                viewHolder.unlikeBtn.setVisibility(View.GONE);
                                viewHolder.likeBtn.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
        Date date = new Date();
        String CURRENT_DATE = formatter.format(date);

        String getTimeAgo = getDaysBtwDates(news.getDate_posted(), CURRENT_DATE);

        viewHolder.userDatePostedText.setText(getTimeAgo);

        viewHolder.postImageView.setXyRatio(news.getXyRatio());

        Picasso.with(viewHolder.postImageView.getContext()).load(news.getPost_image()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.postImageView, new Callback() {
            @Override
            public void onSuccess() {
                viewHolder.loadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Picasso.with(viewHolder.postImageView.getContext()).load(news.getPost_image()).into(viewHolder.postImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        viewHolder.loadingBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        viewHolder.loadingBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        if (!news.getImg_thumbnail().equals("default")) {
            Picasso.with(viewHolder.profileImageView.getContext()).load(news.getImg_thumbnail()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.profileImageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    Picasso.with(viewHolder.profileImageView.getContext()).load(news.getImg_thumbnail()).into(viewHolder.profileImageView);
                }
            });
        } else {
            Picasso.with(viewHolder.profileImageView.getContext()).load(R.drawable.default_img).into(viewHolder.profileImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
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

    private static long getNextDate(String curDate) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return Long.parseLong(format.format(calendar.getTime()));
    }

}

