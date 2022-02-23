package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.googlemaps.model.ApiCall;
import com.example.googlemaps.model.ModelApi;
import com.example.googlemaps.modelPhotos.ApiPhotosCall;
import com.example.googlemaps.modelPhotos.ModelApiPhotos;
import com.example.googlemaps.modelPhotos.Photo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.googlemaps.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private final int AccessLocationRequestCode = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ApiThread apiThread;
    private Retrofit retrofit;
    private Retrofit retrofitPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.sunrise-sunset.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitPhotos = new Retrofit.Builder()
                .baseUrl("https://www.flickr.com/services/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap map) {

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Log.d("Tag", "onMapClick");

                        Log.i("LatLng", "Latitud: " + latLng.latitude + ", longitud: " + latLng.longitude);
                        getAddress(latLng.latitude, latLng.longitude);

                        // API CALL
                        ApiCall apiCall = retrofit.create(ApiCall.class);
                        Call<ModelApi> call = apiCall.getData("" + latLng.latitude, "" + latLng.longitude);

                        // API PHOTOS CALL
                        ApiPhotosCall apiPhotosCall = retrofitPhotos.create(ApiPhotosCall.class);
                        Call<ModelApiPhotos> callPhotos = apiPhotosCall.getData("" + latLng.latitude, "" + latLng.longitude);
                        //Call<ModelApiPhotos> callPhotos = apiPhotosCall.getData();

                        // CALL
                        call.enqueue(new Callback<ModelApi>(){
                            @Override
                            public void onResponse(Call<ModelApi> call, Response<ModelApi> response) {
                                if(response.code()!=200){
                                    Log.i("testApi", "checkConnection");
                                    return;
                                }

                                Log.i("testApi", response.body().getStatus() + " - " + response.body().getResults().getSunrise());
                            }

                            @Override
                            public void onFailure(Call<ModelApi> call, Throwable t) {
                                Log.i("testApi","Failure");
                            }
                        });

                        // CALL PHOTOS
                        callPhotos.enqueue(new Callback<ModelApiPhotos>(){
                            @Override
                            public void onResponse(Call<ModelApiPhotos> callPhotos, Response<ModelApiPhotos> response) {
                                if(response.code()!=200){
                                    Log.i("testApiPhotos", "checkConnection");
                                    return;
                                }

                                ArrayList<Photo> photos = new ArrayList<Photo>();
                                photos = response.body().getPhotos().getPhoto();
                                ArrayList<String> photosUrls = new ArrayList<String>();

                                if (photos.size() == 0) {
                                    Log.i("testApiPhotos", "Error: zero photos");
                                } else {
                                    for (int i = 0; i < 5; i++) {
                                        Photo photo = photos.get(i);
                                        Log.i("testApiPhotos", response.body().getStat() + " - " + photo);
                                        String url = "https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() +  "_" + photo.getSecret() + "_w.jpg";
                                        photosUrls.add(url);
                                    }

                                    Intent intent = new Intent(MapsActivity.this, PageView.class);
                                    intent.putExtra("photosurls", photosUrls);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onFailure(Call<ModelApiPhotos> callPhotos, Throwable t) {
                                Log.i("testApiPhotos","Failure");
                            }
                        });

                        apiThread = new ApiThread( "" + latLng.latitude, "" + latLng.longitude);
                        apiThread.execute();

                        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker set"));
                    }
                });

                map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int reason) {
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            Log.d("Tag", "onCameraMoveStarted");

                        }
                    }
                });

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d("Tag", "onMarkerClick");
                        LatLng latLng = marker.getPosition();
                        getAddress(latLng.latitude, latLng.longitude);
                        marker.remove();

                        return true;
                    }
                });
            }
        });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, AccessLocationRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AccessLocationRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                enableMyLocation();
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        enableMyLocation();
    }

    public void getAddress(double lat, double lng) {
        try {
            //Declare Geocoder
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());

            //Geocoder transforms latitude and longitude to street address
            //The address is stored inside a List of addresses
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);

            if (addresses.isEmpty()) {
                //No address stored
                Toast.makeText(this, "No s’ha trobat informació", Toast.LENGTH_LONG).show();
            } else {
                if (addresses.size() > 0) {
                    //Get address parameters and put them inside a string
                    String msg =addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    //Show string with address parameters
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch(Exception e){
            Toast.makeText(this, "No Location Name Found", Toast.LENGTH_LONG).show();
        }
    }

}