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
import com.codepath.bigheartapp.helpers.FetchResults;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.model.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsFragment extends Fragment implements OnMapReadyCallback, FetchResults {

    // Set variables for the map fragment
    GoogleMap mGoogleMap;
    MapView mapView;
    View view;
    public static Location mCurrentLocation;
    private final int MY_LOCATION_REQUEST_CODE = 130;

    // Set variable to connect to parse database
    private final static String KEY_LOCATION = "location";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        // Inflates and defines the xml file for the fragment
        view = inflater.inflate(R.layout.fragment_maps, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) view.findViewById(R.id.mapView);

        // Checks if mapView is null; if mapView exists, creates the view
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        // Set Google API key
        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {

            // KEY_LOCATION was found in the Bundle, so mCurrentLocation is not null
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // finds the location of user
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

        // markers are drawn at the location specified by the user
        drawMarkers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getActivity().onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
            moveCamera();
        } else {
            Toast.makeText(getContext(), "App does not have access to user's location", Toast.LENGTH_LONG).show();
        }
    }

    // Function to find the location set by the user
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
                            drawCircle();
                            drawMarkers();
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

    private void drawCircle() {
        LatLng userLocation = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(userLocation).radius(5000); // In meters

        mGoogleMap.addCircle(circleOptions);

        return;
    }

    public void moveCamera() {
        if (mCurrentLocation != null) {
            LatLng userLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraPosition cameraPosition = CameraPosition.builder().target(userLocation).zoom(16).bearing(0).tilt(45).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(getContext(), "Could not find current location", Toast.LENGTH_LONG).show();
        }
    }

    public void onLocationChanged(Location location) {

        // GPS may be turned off; nothing happens
        if (location == null) {
            return;
        }

        // If not null, then set current location to changed location
        mCurrentLocation = location;
    }

    public void drawMarkers() {
        FragmentHelper fragmentHelper = new FragmentHelper(getPostQuery());
        fragmentHelper.fetchPosts(this);
    }

    @Override
    public void onFetchSuccess(List<Post> objects) {
        for (int i = 0; i < objects.size(); i++) {
            try {
                // Sets the latitude and longitude of the posts' locations
                Double latitude = objects.get(i).getLocation().getLatitude();
                Double longitude = objects.get(i).getLocation().getLongitude();
                LatLng pos = new LatLng(latitude, longitude);
                BitmapDescriptor coloredIcon;
                if (objects.get(i).getIsEvent()) {
                    // Events have a blue icon
                    coloredIcon = BitmapDescriptorFactory.defaultMarker(305);

                } else {
                    coloredIcon = BitmapDescriptorFactory.defaultMarker(187);
                }
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(objects.get(i).getUser().fetchIfNeeded().getUsername())
                        .icon(coloredIcon)
                        .snippet(objects.get(i).getDescription()));
            } catch (ParseException er) {
                er.printStackTrace();
            }
        }
    }

    @Override
    public void onFetchFailure() {
        Toast.makeText(getContext(), "Failed to fetch markers", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFetchFinish() {

    }

    @Override
    public Post.Query getPostQuery() {
        // get current user
        ParseUser currentUser = ParseUser.getCurrentUser();

        // query for list of post objects unique to current user
        Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();
        postQuery.whereEqualTo("userId", currentUser);
        return postQuery;
    }
}