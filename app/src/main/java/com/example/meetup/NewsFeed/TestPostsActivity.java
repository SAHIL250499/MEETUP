package com.example.meetup.NewsFeed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.meetup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.theophrast.ui.widget.SquareImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class TestPostsActivity extends AppCompatActivity {

    private Button storyBtn, postStoryBtn;
    private SquareImageView imageView;
    private Uri photoUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private StorageReference storageReference, postStoryRef;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_posts);

        storyBtn = findViewById(R.id.new_story_btn);
        postStoryBtn = findViewById(R.id.post_story_btn);

        imageView = findViewById(R.id.imageView2);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        rootRef = FirebaseDatabase.getInstance().getReference().child("Stories");

        progressDialog = new ProgressDialog(this);

        storyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryintent();
            }
        });

        postStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(TestPostsActivity.this, photoUri.toString(), Toast.LENGTH_SHORT).show();
                progressDialog.setTitle("Posting");
                progressDialog.setMessage("Please Wait Your Story Is Ready To Be Featured");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Random random = new Random();
                String post_image_name = String.valueOf(random.nextInt(1000000000)) + String.valueOf(random.nextInt(1000000000)) + ".jpg";
                final String current_user = mAuth.getCurrentUser().getUid();

                Bitmap compressed_image = null;
                File post_filepath = new File(photoUri.getPath());
                try {
                    compressed_image = new Compressor(getApplicationContext())                            .setQuality(60)
                            .compressToBitmap(post_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressed_image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                final byte[] data_bytes_array = baos.toByteArray();

                postStoryRef = storageReference.child("story_images/" + current_user).child(post_image_name);

                UploadTask task = postStoryRef.putBytes(data_bytes_array);
                task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            postStoryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String story_url = uri.toString();
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
                                    Date date = new Date();
                                    String CURRENT_DATE = formatter.format(date);
                                    final String CURRENT_TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                                    Map storiesMap = new HashMap();

                                    storiesMap.put("story_image", story_url);
                                    storiesMap.put("date_posted", CURRENT_DATE);
                                    storiesMap.put("timestamp", CURRENT_TIMESTAMP);
                                    storiesMap.put("posted_by", current_user);

                                    rootRef.child(current_user)
                                            .child(CURRENT_TIMESTAMP)
                                            .setValue(storiesMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(TestPostsActivity.this, "Post Uploaded Successfully!", Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                photoUri = result.getUri();
                Picasso.with(getApplicationContext()).load(photoUri).into(imageView);
                storyBtn.setVisibility(View.GONE);
                postStoryBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startGalleryintent() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(9, 16)
                .start(TestPostsActivity.this);
    }
}
