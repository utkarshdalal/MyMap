package com.example.mymap;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.R;
import android.location.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyMap extends Activity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	private GoogleMap mMap;
	private Circle circle;
	private TextView mAddress;
	private Location mLocation;
	private Marker mMarker;
	private LocationClient mLocationClient;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private Set<String> adds = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Setting up the map*/
        setContentView(R.layout.activity_map_fragment);
        setUpMapIfNeeded();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(37.8717,-122.2728)).title("This is Berkeley").snippet("Berkeley, CA").draggable(true)
        	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.setMyLocationEnabled(true);
        
        mLocationClient = new LocationClient(this, this, this);
        mLocation = new Location("");
        //mLoc.setLatitude(37.8717);
        //mLoc.setLongitude(-122.2728);
        
        /*Draw the circle*/
        CircleOptions circleOptions = new CircleOptions()
        .center(new LatLng(37.8717,-122.2728))
        .radius(804.672); // In meters
        circle = mMap.addCircle(circleOptions);
        circle.setFillColor(0x2000ffff);
        
        /*What to do when the marker is dragged*/
        OnMarkerDragListener omdg = new OnMarkerDragListener(){ 
            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Called when the marker is dropped down.
            	marker.setTitle(marker.getPosition().latitude +", " + marker.getPosition().longitude);
            	marker.setSnippet("Area");
            	circle.setCenter(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                circle.setVisible(true);
                try {
					getAddress();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
			@Override
			public void onMarkerDrag(Marker marker) {
				// TODO Auto-generated method stub
                
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
				// TODO Auto-generated method stub
				circle.setVisible(false);
				Toast.makeText(getApplicationContext(),"Put me down!", Toast.LENGTH_LONG).show();
			}
        };
        mMap.setOnMarkerDragListener(omdg);
    }
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                                .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.

            }
        }
    }
    
    public void getLocation() {
    	mLocation.setLatitude(mMarker.getPosition().latitude);
    	mLocation.setLongitude(mMarker.getPosition().longitude);
    }
    
    @SuppressLint("NewApi")
    public void getAddress() throws IOException {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, "No geocoder", Toast.LENGTH_LONG).show();
            return;
        }
        // Start the background task
        getLocation();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Create a list to contain the result address
        /*List <Address> tempAddresses = null;
        for (int i = -5; i < 5; i ++){
        	for (int j = -5; j < 5; j ++){
        		tempAddresses = geocoder.getFromLocation(mLocation.getLatitude() + i*0.0003,
                        mLocation.getLongitude() + j*0.0003, 1);
        		for (int k = 0; k < tempAddresses.size(); k ++){
        			Address tempAddress = tempAddresses.get(k);
        			String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        tempAddress.getMaxAddressLineIndex() > 0 ?
                                tempAddress.getAddressLine(0) : "",
                        // Locality is usually a city
                        tempAddress.getLocality(),
                        // The country of the address
                        tempAddress.getCountryName());
                	Toast.makeText(getApplicationContext(),"Nearby address: " + addressText, Toast.LENGTH_LONG).show();
        			//adds.add(tempAddresses.get(k));
        		}
        	}
        }*/
        String neighbourhood = getAddressHelper(geocoder);
        Toast.makeText(getApplicationContext(),"Current neighbourhood is: " + neighbourhood, Toast.LENGTH_LONG).show();
    }
    
    public String getAddressHelper(Geocoder geocoder) throws IOException{
    	List <Address> addresses = null;
    	addresses = geocoder.getFromLocation(mLocation.getLatitude(),
                mLocation.getLongitude(), 1);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String neighbourhood = address.getSubLocality();
            if (neighbourhood != null){
            	return neighbourhood;
            }
        }
        addresses = geocoder.getFromLocation(mLocation.getLatitude() + 0.001,
                mLocation.getLongitude(), 1);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String neighbourhood = address.getSubLocality();
            if (neighbourhood != null){
            	return neighbourhood;
            }
        }
        addresses = geocoder.getFromLocation(mLocation.getLatitude() - 0.001,
                mLocation.getLongitude(), 1);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String neighbourhood = address.getSubLocality();
            if (neighbourhood != null){
            	return neighbourhood;
            }
        }
        addresses = geocoder.getFromLocation(mLocation.getLatitude() + 0.001,
                mLocation.getLongitude() + 0.001, 1);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String neighbourhood = address.getSubLocality();
            if (neighbourhood != null){
            	return neighbourhood;
            }
        }
        addresses = geocoder.getFromLocation(mLocation.getLatitude() - 0.001,
                mLocation.getLongitude() - 0.001, 1);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String neighbourhood = address.getSubLocality();
            if (neighbourhood != null){
            	return neighbourhood;
            }
        }
        return "No neighbourhood found";
    }
    
    /*Functions for location client*/
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
    
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    /*The following three functions are related to setting up the location client*/
    
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		// Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        try {
			getAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		// Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
		
	}
}