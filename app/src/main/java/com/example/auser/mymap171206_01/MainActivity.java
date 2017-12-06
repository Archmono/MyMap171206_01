package com.example.auser.mymap171206_01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener,OnMapReadyCallback{
    static final int MIN_TIME = 500;
    static final float MIN_DIS = 5;
    LocationManager mgr;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    GoogleMap map;
    LatLng currentPoint;
    TextView tvshow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvshow = (TextView)findViewById(R.id.textView1);
        mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkPermission();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void checkPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},200);
        }
    }

    private void enableLocationUpdates(boolean isTurnOn){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(isTurnOn){
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(!isGPSEnabled && !isNetworkEnabled){
                    Toast.makeText(getApplicationContext(), "無網路功能", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "定位中...", Toast.LENGTH_SHORT).show();
                    if(isGPSEnabled){
                        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIS,this);
                    }
                    if(isNetworkEnabled){
                        mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DIS,this);
                    }
                }
            } else {
                mgr.removeUpdates(this);
            }
        }
    }

    //enable==============================
    @Override
    protected void onResume() {
        super.onResume();
        enableLocationUpdates(true);
    }
    //enable==============================

    //enable==============================
    @Override
    protected void onPause() {
        super.onPause();
        enableLocationUpdates(false);
    }
    //enable==============================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mark:
                map.clear();
                map.addMarker(new MarkerOptions().position(map.getCameraPosition().target).title(getAddress()));
                break;
            case R.id.settle:
                item.setChecked(!item.isChecked());
                if(item.isChecked()){
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }else{
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.traffic:
                item.setChecked(!item.isChecked());
                map.setTrafficEnabled(item.isChecked());
                break;
            case R.id.currpoint:
                map.animateCamera(CameraUpdateFactory.newLatLng(currentPoint));
                break;
            case R.id.setup:
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
                break;
            case R.id.quit:
                finish();
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            tvshow.setText(String.format("緯度:%.4f 經度:%.4f ",location.getLatitude(), location.getLongitude()));
            currentPoint = new LatLng(location.getLatitude(),location.getLongitude());
            if(map != null){
                map.animateCamera(CameraUpdateFactory.newLatLng(currentPoint));
                map.addMarker(new MarkerOptions().position(currentPoint).title("目前位置"));
            }
        } else {
            tvshow.setText("Error, Map no Settle");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //===================================================================================
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.moveCamera(CameraUpdateFactory.zoomTo(7));
    }
    String getAddress(){
        LatLng target = map.getCameraPosition().target;
        try{
            List<Address> addressList = new Geocoder(this, Locale.getDefault()).getFromLocation(target.latitude,target.longitude,1);
            if(addressList==null && addressList.size()==0){
                return "查無地址";

            }else{
                String straddress  ="";
                Address address = addressList.get(0);
                for(int i =0;i<=address.getMaxAddressLineIndex();i++){
                    straddress+=address.getAddressLine(i)+"\n";
                }
                return straddress;

            }
        }catch (Exception e){
            return String.format("查無地址 :  緯度：%.4f　　經度：　%.4f",target.latitude, target.longitude);
        }

    }
}
