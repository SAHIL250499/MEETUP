package com.example.meetup.LocationPackage;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetup.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private int count = 0;
    private double avg_lat = 0, avg_lng = 0;
    private boolean mapLoaded = false;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        mapView = view.findViewById(R.id.user_list_map);
        initMap(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    private void initMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if (savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle("BundleKey");
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle("BundleKey");
        if (mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle("BundleKey", mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        String curr = mAuth.getCurrentUser().getUid();

        rootRef.child("Friends").child(curr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()){
                    String f_uid = d.getKey();

                    rootRef.child("Users").child(f_uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("LAT") && dataSnapshot.hasChild("LNG")){

                                String LAT = dataSnapshot.child("LAT").getValue().toString();
                                String LNG = dataSnapshot.child("LNG").getValue().toString();
                                String username = dataSnapshot.child("username").getValue().toString();
                                String image = dataSnapshot.child("img_thumbnail").getValue().toString();

                                avg_lat += Double.parseDouble(LAT);
                                avg_lng += Double.parseDouble(LNG);
                                count++;

                                mapLoaded = true;

                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(
                                                Double.parseDouble(LAT),
                                                Double.parseDouble(LNG))
                                        )
                                        .title(username)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                );
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootRef.child("Users").child(curr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("LAT") && dataSnapshot.hasChild("LNG")){
                    String LAT = dataSnapshot.child("LAT").getValue().toString();
                    String LNG = dataSnapshot.child("LNG").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String image = dataSnapshot.child("img_thumbnail").getValue().toString();

                    avg_lat += Double.parseDouble(LAT);
                    avg_lng += Double.parseDouble(LNG);
                    count++;

                    mapLoaded = true;

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(
                                    Double.parseDouble(LAT),
                                    Double.parseDouble(LNG))
                            )
                            .title(username)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        if (mapLoaded) {
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(avg_lat / count , avg_lng / count))
//                    .zoom(10)
//                    .build();
//            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(19.0825223,72.7410986))
                    .zoom(9)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }

//        try {
//            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style));
//        } catch (Resources.NotFoundException e){
//            Log.e(TAG, "Can't find style. Error: ", e);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
