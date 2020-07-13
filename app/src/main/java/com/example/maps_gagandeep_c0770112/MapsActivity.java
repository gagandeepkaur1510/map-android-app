package com.example.maps_gagandeep_c0770112;

import androidx.fragment.app.FragmentActivity;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private List<Marker> markers;
    private List<Polyline> polylines;
    private Marker previousMarker = null;
    private static final int POLYGON_SIDES = 4;
    private Marker infomarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markers = new ArrayList<>();
        polylines = new ArrayList<>();
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.641914, -79.387143),10f));
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                StringBuilder title = new StringBuilder();
                StringBuilder desc = new StringBuilder();
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                try {
                    Address address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0);
                    title.append(address.getThoroughfare()+" "+address.getSubThoroughfare()+" "+address.getPostalCode());
                    desc.append(address.getLocality()+" "+address.getAdminArea());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(markers.size() == POLYGON_SIDES)
                {
                    markers.remove(0).remove();
                }
                MarkerOptions options = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker)).title(title.toString()).snippet(desc.toString()).draggable(true);
                Marker marker = mMap.addMarker(options);
                markers.add(marker);
                if(previousMarker!=null)
                {
                    PolylineOptions polylineOptions = new PolylineOptions().add(marker.getPosition(),previousMarker.getPosition()).color(Color.RED);
                    polylines.add(mMap.addPolyline(polylineOptions));
                }
                previousMarker = marker;
                if(markers.size() == POLYGON_SIDES)
                {
                    PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.argb((int) ((float)((float) 35/ (float) 100)*255),0,255,0)).strokeColor(Color.RED);
                    for(Marker marker1: markers)
                    {
                        polygonOptions.add(marker1.getPosition());
                    }
                    mMap.addPolygon(polygonOptions);
                }
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.d(TAG,"onPolylineClick: "+polyline);
                if (infomarker != null)
                {
                    infomarker.remove();
                }
                double lat = (polyline.getPoints().get(0).latitude + polyline.getPoints().get(1).latitude)/2;
                double lng = (polyline.getPoints().get(0).longitude + polyline.getPoints().get(1).longitude)/2;
                double distance = distance(polyline.getPoints().get(0).latitude, polyline.getPoints().get(0).longitude,polyline.getPoints().get(1).latitude,polyline.getPoints().get(1).longitude);
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(new LatLng(lat,lng)).title(distance+" km");
                infomarker = mMap.addMarker(markerOptions);
                infomarker.showInfoWindow();
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                if (infomarker != null)
                {
                    infomarker.remove();
                }
                double distance = 0;
                double lat = 0;
                double lng = 0;
                LatLng latLng1 = null;
                for(LatLng latLng: polygon.getPoints())
                {
                    if(latLng1!=null)
                    {
                        lat = (latLng1.latitude + latLng.latitude)/2;
                        lng = (latLng1.longitude + latLng.longitude)/2;
                        distance += distance(latLng.latitude, latLng.longitude,latLng1.latitude,latLng1.longitude);
                    }
                    latLng1 = latLng;
                }
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(new LatLng(lat,lng)).title(distance+" km");
                infomarker = mMap.addMarker(markerOptions);
                infomarker.showInfoWindow();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG,"onMarkerDragEnd: "+marker);
                mMap.clear();
                List<Marker> markers = new ArrayList<>();
                for(Marker marker1: MapsActivity.this.markers)
                {
                    MarkerOptions markerOptions = new MarkerOptions().position(marker1.getPosition()).title(marker1.getTitle()).snippet(marker1.getSnippet()).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
                    markers.add(mMap.addMarker(markerOptions));
                    mMap.addMarker(markerOptions);
                }
                MapsActivity.this.markers = markers;
                PolylineOptions polylineOptions;
                polylines.clear();
                Marker marker2 = null;
                for(Marker marker1: MapsActivity.this.markers)
                {
                    if(marker2 != null)
                    {
                        polylineOptions = new PolylineOptions().color(Color.RED).add(marker1.getPosition(),marker2.getPosition());
                        polylines.add(mMap.addPolyline(polylineOptions));
                    }
                    marker2 = marker1;
                }
                if(markers.size() == POLYGON_SIDES)
                {
                    PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.argb((int) ((float)((float) 35/ (float) 100)*255),0,255,0)).strokeColor(Color.RED);
                    for(Marker marker1: markers)
                    {
                        polygonOptions.add(marker1.getPosition());
                    }
                    mMap.addPolygon(polygonOptions);
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                for (Marker marker : markers)
                {
                    if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05)
                    {
                        markers.remove(marker);
                        break;
                    }
                }

                mMap.clear();
                List<Marker> markers = new ArrayList<>();
                for(Marker marker1: MapsActivity.this.markers)
                {
                    MarkerOptions markerOptions = new MarkerOptions().position(marker1.getPosition()).title(marker1.getTitle()).snippet(marker1.getSnippet()).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
                    markers.add(mMap.addMarker(markerOptions));
                    mMap.addMarker(markerOptions);
                }
                MapsActivity.this.markers = markers;
                PolylineOptions polylineOptions;
                polylines.clear();
                Marker marker2 = null;
                for(Marker marker1: MapsActivity.this.markers)
                {
                    if(marker2 != null)
                    {
                        polylineOptions = new PolylineOptions().color(Color.RED).add(marker1.getPosition(),marker2.getPosition());
                        polylines.add(mMap.addPolyline(polylineOptions));
                    }
                    marker2 = marker1;
                }
                if(markers.size() == POLYGON_SIDES)
                {
                    PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.argb((int) ((float)((float) 35/ (float) 100)*255),0,255,0)).strokeColor(Color.RED);
                    for(Marker marker1: markers)
                    {
                        polygonOptions.add(marker1.getPosition());
                    }
                    mMap.addPolygon(polygonOptions);
                }
            }
        });
    }

    double distance(double lat1, double lon1, double lat2, double lon2)
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }
}