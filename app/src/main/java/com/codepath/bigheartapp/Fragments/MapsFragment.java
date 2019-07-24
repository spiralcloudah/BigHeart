package com.codepath.bigheartapp.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView mapView;
    View view;
    Location mCurrentLocation;
    private final int MY_LOCATION_REQUEST_CODE = 130;
    private final static String KEY_LOCATION = "location";


    //required empty constructor
    public MapsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        view = inflater.inflate(R.layout.fragment_maps, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

        drawMarkers(mGoogleMap);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        getActivity().onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            Toast.makeText(getContext(), "App does not have access to user's location", Toast.LENGTH_LONG).show();
        }
    }


    @SuppressWarnings({"MissingPermission"})
    void getMyLocation() {
        mGoogleMap.setMyLocationEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                            moveCamera();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

    }

    public void moveCamera() {
        if(mCurrentLocation != null) {
            LatLng userLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraPosition cameraPosition = CameraPosition.builder().target(userLocation).zoom(16).bearing(0).tilt(45).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(getContext(), "Could not find current location", Toast.LENGTH_LONG).show();
        }
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }

        mCurrentLocation = location;
    }

    public void drawMarkers(final GoogleMap mGoogleMap) {

        // get current user
        ParseUser currentUser = ParseUser.getCurrentUser();

        // query for list of post objects unique to current user
        Post.Query postQuery = new Post.Query();

        postQuery.getTop().withUser();

        postQuery.whereEqualTo("userId",currentUser);

        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    Log.d("Maps Fragment", "Success!");
                    for (int i = 0; i < objects.size(); i++) {
                        try {
                            Double latitude = objects.get(i).getLocation().getLatitude();
                            Double longitude = objects.get(i).getLocation().getLongitude();
                            LatLng pos = new LatLng(latitude,longitude);
                            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(objects.get(i).getUser().fetchIfNeeded().getUsername())
                                    .snippet(objects.get(i).getDescription()));
                        } catch (ParseException er ) {
                            er.printStackTrace();
                        }

                    }
                } else {
                    Log.d("Maps Fragment", "Failure");
                    e.printStackTrace();
                }
            }
        });



    }



}

