package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.meetup.ChatsPackage.ChatListActivity;
import com.example.meetup.FriendsPackage.FriendRequestsFragment;
import com.example.meetup.LocationPackage.LocationFragment;
import com.example.meetup.LocationPackage.MapsActivity;
import com.example.meetup.NewsFeed.TestPostsActivity;
import com.example.meetup.PostsPackage.PostsFragment;
import com.example.meetup.ProfilePackage.ProfileActivity;
import com.example.meetup.ProfilePackage.ProfileFragment;
import com.example.meetup.SearchPackage.SearchFragment;
import com.example.meetup.UsersPackage.UsersActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static maes.tech.intentanim.CustomIntent.customType;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ID = 44;
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private BottomNavigationView mainNav;
    private DatabaseReference mUserDatabase;

    private Fragment postsFragment, profileFragment, friendReqFragment, locationFragment, searchFragment;

    private GoogleSignInClient mGoogleSignInClient;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean permflag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mainNav = findViewById(R.id.main_nav);

        mToolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.main_action_bar, null);

        actionBar.setCustomView(action_bar_view);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        postsFragment = new PostsFragment();
        profileFragment = new ProfileFragment();
        friendReqFragment = new FriendRequestsFragment();
        locationFragment = new LocationFragment();
        searchFragment = new SearchFragment();

        replaceFragment(postsFragment);

        mainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        replaceFragment(postsFragment);
                        return true;
                    case R.id.nav_search:
                        replaceFragment(searchFragment);
                        return true;
                    case R.id.nav_notification:
                        replaceFragment(friendReqFragment);
                        return true;
                    case R.id.nav_profile:
                        replaceFragment(profileFragment);
                        return true;
                    case R.id.nav_loc:
//                        startActivity(new Intent(MainActivity.this, MapsActivity.class));
                        replaceFragment(locationFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });
        onResume();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        } else {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (checkPermissions()) {
                getLastLocation();
            } else {
                requestPermissions(permflag);
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");
            Date date = new Date();
            String CURRENT_DATE = formatter.format(date);

            Map updateHashmap = new HashMap();
            updateHashmap.put("online_at", CURRENT_DATE);

            mUserDatabase.child(currentUser.getUid()).updateChildren(updateHashmap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void sendToStart() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        customType(MainActivity.this, "right-to-left");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_logout_btn:
                signOutFromAll();
                return true;
            case R.id.chat_messages:
                startActivity(new Intent(MainActivity.this, ChatListActivity.class));
                return true;
            case R.id.all_users:
                startActivity(new Intent(MainActivity.this, UsersActivity.class));
                return true;
            default:
                return false;
        }
    }

    private void signOutFromAll() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sendToStart();
            }
        });
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions(boolean flag) {
        if (flag) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ID
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                permflag = false;
                Map updateHashmap = new HashMap();
                updateHashmap.put("LAT", null);
                updateHashmap.put("LNG", null);
                mUserDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(updateHashmap);
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());

            Map updateHashmap = new HashMap();
            updateHashmap.put("LAT", lat);
            updateHashmap.put("LNG", lng);
            mUserDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(updateHashmap);
        }
    };

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    String lat = String.valueOf(location.getLatitude());
                                    String lng = String.valueOf(location.getLongitude());

                                    Map updateHashmap = new HashMap();
                                    updateHashmap.put("LAT", lat);
                                    updateHashmap.put("LNG", lng);
                                    mUserDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(updateHashmap);
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            requestPermissions(permflag);
        }
    }
}
