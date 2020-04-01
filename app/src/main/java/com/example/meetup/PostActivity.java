package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private Toolbar mToolbar;
    private Button selectImage, uploadPost;
    private EditText addDesc;
    private CheckBox checkBox;
    private SquareImageView previewImage;
    private StorageReference mPostImageStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference mPostsDatabase;
    private ProgressDialog progressDialog;

    private Uri photoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = findViewById(R.id.post_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uploadPost = findViewById(R.id.upload_post);
        addDesc = findViewById(R.id.add_desc);

        checkBox = findViewById(R.id.post_type_story);

        previewImage = findViewById(R.id.preview_image);

        mPostImageStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        mPostsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        startGalleryintent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
//            if (photoUri == null){
//
//            }
            startActivity(new Intent(PostActivity.this, MainActivity.class));
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri result_uri = result.getUri();
                photoUri = result_uri;
                final File post_filepath = new File(result_uri.getPath());

                Picasso.with(getApplicationContext()).load(result_uri).into(previewImage);
                previewImage.setVisibility(View.VISIBLE);

                uploadPost.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        progressDialog.setTitle("Posting");
                        progressDialog.setMessage("Please Wait Your Post Is Ready To Be Featured");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        Random random = new Random();
                        String post_image_name = String.valueOf(random.nextInt(1000000000)) + String.valueOf(random.nextInt(1000000000)) + ".jpg";
                        final String current_user = mAuth.getCurrentUser().getUid();

                        Bitmap compressed_image = null;
                        try {
                            compressed_image = new Compressor(getApplicationContext())
                                    .setMaxWidth(200)
                                    .setMaxHeight(200)
                                    .setQuality(10)
                                    .compressToBitmap(post_filepath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressed_image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        final byte[] data_bytes_array = baos.toByteArray();

                        Bitmap post_compressed = null;
                        try {
                            post_compressed = new Compressor(getApplicationContext())
                                    .setQuality(30)
                                    .compressToBitmap(post_filepath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream pbaos = new ByteArrayOutputStream();
                        post_compressed.compress(Bitmap.CompressFormat.JPEG, 40, pbaos);
                        final byte[] p_data_ba = pbaos.toByteArray();

                        final int width = compressed_image.getWidth();
                        final int height = compressed_image.getHeight();

                        final StorageReference postImageRef = mPostImageStorage.child("posts_images/" + current_user).child(post_image_name);
                        final StorageReference postImageThumbRef = mPostImageStorage.child("posts_images/thumbs/" + current_user).child(post_image_name);

                        postImageRef.putBytes(p_data_ba).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                postImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String download_url = uri.toString();

                                        UploadTask uploadTask = postImageThumbRef.putBytes(data_bytes_array);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    postImageThumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String thumbnail_downloadurl = uri.toString();

                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
                                                            Date date = new Date();
                                                            String CURRENT_DATE = formatter.format(date);
                                                            final String CURRENT_TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                                                            Map postsHashmap = new HashMap();
                                                            postsHashmap.put("post_image", download_url);
                                                            postsHashmap.put("post_image_thumbnail", thumbnail_downloadurl);
                                                            postsHashmap.put("date_posted", CURRENT_DATE);
                                                            postsHashmap.put("timestamp", CURRENT_TIMESTAMP);
                                                            postsHashmap.put("posted_by", current_user);
                                                            postsHashmap.put("likes", 0);
                                                            postsHashmap.put("comments",0);
                                                            postsHashmap.put("width", width);
                                                            postsHashmap.put("height", height);

                                                            if (!TextUtils.isEmpty(addDesc.getText().toString())){
                                                                postsHashmap.put("post_desc",addDesc.getText().toString());
                                                            } else{
                                                                postsHashmap.put("post_desc", "");
                                                            }

                                                            if (checkBox.isChecked()){
                                                                postsHashmap.put("post_type", "image_story");
                                                            } else {
                                                                postsHashmap.put("post_type", "image_post");
                                                            }

                                                            mPostsDatabase.child(current_user).child(CURRENT_TIMESTAMP).setValue(postsHashmap)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            progressDialog.dismiss();
                                                                            Toast.makeText(PostActivity.this, "Post Uploaded Successfully!", Toast.LENGTH_LONG).show();
                                                                            startActivity(new Intent(PostActivity.this, MainActivity.class));
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(PostActivity.this, "Upload Error!", Toast.LENGTH_LONG).show();
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
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void startGalleryintent() {
//        Intent galleryIntent = new Intent();
//        galleryIntent.setType("image/*");
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
}