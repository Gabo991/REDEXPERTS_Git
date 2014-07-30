package com.example.redexperts_gabriel_kudela;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

public class LocationActivity extends FragmentActivity implements LocationListener, OnMarkerClickListener {

    private GoogleMap mMap;
    private LatLng pinLocation;
    private String imageURL;
    private String textJSON;

    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        getExtras();
        enableMyLocation();
        setUpMapIfNeeded();
    }

    private void enableMyLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                getDistance(new LatLng(location.getLatitude(), location.getLongitude()), pinLocation);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enable providing location via network!", Toast.LENGTH_LONG).show();
            ((TextView) findViewById(R.id.distanceTextView)).setText("No information about current location.");
        }
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pinLocation = new LatLng(
                    Double.parseDouble(extras.getString("latitude")),
                    Double.parseDouble(extras.getString("longitude")));
            imageURL = extras.getString("imageURL");
            textJSON = extras.getString("text");
        }
    }

    private void getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        DecimalFormat df = new DecimalFormat("#.###");
        ((TextView) findViewById(R.id.distanceTextView)).setText("Distance from the Marker is about: " +
        String.valueOf(df.format(distance / 1000)) + "km.");

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        mMap.addMarker(new MarkerOptions()
                .position(pinLocation)
                .title(textJSON));

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnMarkerClickListener(this);
    }

    public void onLocationChanged(Location location) {
        getDistance(new LatLng(location.getLatitude(), location.getLongitude()), pinLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public class ImageDownloader extends AsyncTask<String, Integer, Drawable> {

        protected Drawable doInBackground(String... params) {
            String url = params[0];

            return GetImageFromUrl(url);
        }

        private Drawable GetImageFromUrl(String url)
        {
            try
            {
                InputStream is = (InputStream) new URL(url).getContent();
                return Drawable.createFromStream(is, "image");
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    class CustomInfoWindowAdapter implements InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            try {
                ((ImageView) view.findViewById(R.id.infowindowImage)).setImageDrawable(new ImageDownloader().execute(imageURL).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            String title = marker.getTitle();
            TextView titleTv = ((TextView) view.findViewById(R.id.infowindowTitle));
            if (title != null) {
                titleTv.setText(title);
            } else {
                titleTv.setText("");
            }
        }
    }
}
