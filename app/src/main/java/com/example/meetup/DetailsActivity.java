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
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class DetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private TextInputLayout displayStatus, displayUsername, displayName;
    private MaterialButton mSaveChangesBtn;
    private ProgressDialog mProgressDialog;
    private CircleImageView profileImageView;
    private ExtendedFloatingActionButton changeImage;
    private static final int GALLERY_PICK = 1;

    private ProgressDialog progressDialog, dataDialog;

    // Firebase
    private String mCurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mRootRef;
    private String OldUsername, OldDisplayName, OldStatus;
    private StorageReference mImageStorage;


    private boolean isUserNameUpdated = false;
    private boolean userNameFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.details_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayStatus = findViewById(R.id.profile_userStatus);
        displayName = findViewById(R.id.profile_displayName);
        displayUsername = findViewById(R.id.profile_userName);

        profileImageView = findViewById(R.id.profile_image);
        changeImage = findViewById(R.id.change_image);

        mSaveChangesBtn = findViewById(R.id.save_changes);

        dataDialog = new ProgressDialog(this);
        dataDialog.setMessage("Loading");
        dataDialog.setCanceledOnTouchOutside(false);
        dataDialog.show();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = mRootRef.child("Users").child(mCurrentUser);

        mProgressDialog = new ProgressDialog(this);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                OldDisplayName = dataSnapshot.child("name").getValue().toString();
                OldStatus = dataSnapshot.child("status").getValue().toString();
                OldUsername = dataSnapshot.child("username").getValue().toString();
                String profile_img = dataSnapshot.child("img_thumbnail").getValue().toString();

                displayName.getEditText().setText(OldDisplayName);
                displayUsername.getEditText().setText(OldUsername);
                displayStatus.getEditText().setText(OldStatus);
                Picasso.with(getApplicationContext()).load(profile_img).into(profileImageView);

                dataDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map updateUserMap = new HashMap();

                String new_name = displayName.getEditText().getText().toString();
                String new_status = displayStatus.getEditText().getText().toString();
                final String new_username = displayUsername.getEditText().getText().toString().trim();

                if (!new_name.equals(OldDisplayName)){
                    updateUserMap.put("name", new_name);
                }
                if (!new_status.equals(OldStatus)){
                    updateUserMap.put("status", new_status);
                }
                if (!new_username.equals(OldUsername)){
                    if (validateUsername(new_username)){
                        mRootRef.child("Usernames").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(new_username)){
                                    userNameFlag = true;
                                } else {
                                    Toast.makeText(DetailsActivity.this, new_username + " is already taken!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(DetailsActivity.this, "Username should consist of lowercase letters, numbers and underscores and should be in the range of 4 to 18 characters", Toast.LENGTH_LONG).show();
                    }
                }

                mUserDatabase.updateChildren(updateUserMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            if (userNameFlag){
                                mRootRef.child("Usernames").child(OldUsername).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            mRootRef.child("Usernames").child(new_username).child("user_id").setValue(mCurrentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        mUserDatabase.child("username").setValue(new_username).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                isUserNameUpdated = false;
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(DetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            Toast.makeText(DetailsActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    private boolean validateUsername(String new_username) {
        return (Pattern.matches("^[a-z0-9_]+$", new_username) && new_username.length() > 3 && new_username.length() < 19);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please Wait While The Image Is Being Uploaded");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                File thumb_filepath = new File(resultUri.getPath());
                final String uid = mCurrentUser;
                final String image_name = uid + ".jpg";

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(10)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] data_bytes_array = baos.toByteArray();

                final StorageReference file_path = mImageStorage.child("profile_images").child(image_name);
                final StorageReference thumbnail_file_path = mImageStorage.child("profile_images").child("thumbs").child(image_name);

                file_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            file_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String download_url = uri.toString();

                                    UploadTask uploadTask = thumbnail_file_path.putBytes(data_bytes_array);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                            if (task.isSuccessful()){
                                                thumbnail_file_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String thumbnail_download_url = uri.toString();
                                                        Map updateHashmap = new HashMap();
                                                        updateHashmap.put("image",download_url);
                                                        updateHashmap.put("img_thumbnail", thumbnail_download_url);

                                                        mUserDatabase.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(DetailsActivity.this, "Image Uploaded Successfully!", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            } else{
                                                Toast.makeText(DetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        } else{
                            progressDialog.dismiss();
                            Toast.makeText(DetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
