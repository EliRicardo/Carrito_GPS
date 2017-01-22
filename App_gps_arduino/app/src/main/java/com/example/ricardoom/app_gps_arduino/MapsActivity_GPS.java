package com.example.ricardoom.app_gps_arduino;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.vision.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;


import android.bluetooth.BluetoothAdapter;
public class MapsActivity_GPS extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest ;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private GoogleMap mMap;
    Marker Putin;
    Marker Putin_meta;
    private LatLng latLng;
    private LatLng latLng_previo;
    private LatLng latLng_meta;
    Geocoder geocoder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps_activity__gps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        setUpMapIfNeeded();

        btnListPairedDevices = (Button)findViewById(R.id.btnConectar);
        stateBluetooth = (TextView)findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBlueToothState();

        btnListPairedDevices.setOnClickListener(btnListPairedDevicesOnClickListener);
        ///miBluetooth mBluetooth = new miBluetooth();
        ///mBluetooth.prender();
        longitud0 =(TextView)findViewById(R.id.longitud0);
        latitud0 = (TextView)findViewById(R.id.latitud0);

        SharedPreferences myprefs= this.getSharedPreferences("user", MODE_WORLD_READABLE);
        myprefs.edit().putString("session_id", "espera").commit();

        miHilo hilo = new miHilo();
        hilo.start();
    }


    public void UbicarCarrito()
    {
        Putin = mMap.addMarker(new MarkerOptions().position(latLng).title("Carrito!!!"));

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // locacion del carrito!!!!!!!!!!!!!!1
        //LatLng cusco = new LatLng(-13.525,-71.9722);
        //Putin = mMap.addMarker(new MarkerOptions().position(cusco).title("Carrito Putin!!!"));
        //EditText latitud = (EditText)findViewById(R.id.latitud0);
        //EditText longitud = (EditText)findViewById(R.id.longitud0);
        //latitud.setText(Double.toString(cusco.latitude));
        //longitud.setText(Double.toString(cusco.longitude));

        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //mMap.setMyLocationEnabled(true);

    }



    public void setUpMapIfNeeded(){
        if(mMap == null){
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if(mMap != null){
                setUpMap();
            }
        }
    }


    private void setUpMap() {

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                //save current location
                latLng_meta = point;
                EditText latitud = (EditText)findViewById(R.id.latitud1);
                EditText longitud = (EditText)findViewById(R.id.longitud1);
                latitud.setText(Double.toString(point.latitude));
                longitud.setText(Double.toString(point.longitude));
                List<android.location.Address> addresses = new ArrayList<android.location.Address>();
                try {
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                android.location.Address address = addresses.get(0);

                if (address != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                        sb.append(address.getAddressLine(i) + "\n");
                    }
                    Toast.makeText(MapsActivity_GPS.this, sb.toString(), Toast.LENGTH_LONG).show();
                }

                //remove previously placed Marker
                if (Putin_meta != null) {
                    Putin_meta.remove();
                }

                //place marker where user just clicked
                Putin_meta = mMap.addMarker(new MarkerOptions().position(point).title("Meta!!!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                //crearDato();
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                                                    @Override
                                                    public boolean onMyLocationButtonClick() {

                                                        LatLng cusco = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
                                                        Putin = mMap.addMarker(new MarkerOptions().position(cusco).title("Carrito!!!"));
                                                        EditText latitud = (EditText) findViewById(R.id.latitud0);
                                                        EditText longitud = (EditText) findViewById(R.id.longitud0);
                                                        latitud.setText(Double.toString(cusco.latitude));
                                                        longitud.setText(Double.toString(cusco.longitude));
                                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(cusco));
                                                        return false;
                                                    }
                                                }
        );
    }

    public void crearDato()
    {
        TextView longitud1 = (TextView)findViewById(R.id.longitud1);
        TextView latitud1 = (TextView)findViewById(R.id.latitud1);
        //String value =longitud1.getText().toString()+","+latitud1.getText().toString();
        String instrucciones = "w";
        SharedPreferences myprefs= this.getSharedPreferences("user", MODE_WORLD_READABLE);
        myprefs.edit().putString("session_id", instrucciones).commit();
        //mandar dato de meta



    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        EditText latitud = (EditText)findViewById(R.id.latitud0);
        EditText longitud = (EditText)findViewById(R.id.longitud0);


    }

    @Override
    public void onLocationChanged(Location location)
    {
        Putin.remove();
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Carro Putin");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        EditText latitud0 = (EditText)findViewById(R.id.latitud0);
        EditText longitud0 = (EditText)findViewById(R.id.longitud0);
        latitud0.setText(((Double.toString( latLng.latitude))));
        longitud0.setText(((Double.toString( latLng.longitude))));
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PAIRED_DEVICE = 2;

    Button btnListPairedDevices;
    TextView stateBluetooth;
    BluetoothAdapter bluetoothAdapter;


    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            stateBluetooth.setText("Bluetooth NOT support");
        }else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    stateBluetooth.setText("Bluetooth is currently in device discovery process.");
                }else{
                    stateBluetooth.setText("Bluetooth is Enabled.");
                    btnListPairedDevices.setEnabled(true);
                }
            }else{
                stateBluetooth.setText("Bluetooth is NOT Enabled!");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
        }
    }
    private Button.OnClickListener btnListPairedDevicesOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(MapsActivity_GPS.this, ListPairedDevicesActivity.class);

            startActivityForResult(intent, REQUEST_PAIRED_DEVICE);
            //startActivityForResult(intent,1);
        }};
    private Handler mHandler;
    TextView longitud0;
    TextView latitud0;
    String valores[];
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub


        if(requestCode == REQUEST_ENABLE_BT){
            CheckBlueToothState();
        }
        if (requestCode == REQUEST_PAIRED_DEVICE){
            if(resultCode == RESULT_OK){
                Log.e("Log", "dato llego : "+data.getStringExtra("posicion0"));
                valores = data.getStringExtra("posicion0").split(",");
                debugMsg();
                //longitud0.setText(data.getStringExtra("posicion0"));
                //if(latLng_previo!=null && latLng_meta!=null)
                //{
                 //   rotacion(latLng,latLng_previo,latLng_meta);
                   // Log.i("Log", "calculando rotacion!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                   // latLng_previo = latLng;
                //}
                //else{Log.i("Log", "no calculando rotacion!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}

            }
        }
    }

    private class miHilo extends  Thread
    {
        Double dato;
        public miHilo(){dato=0.0;}
        public void run()
        {

            while(true){
                if(latLng_previo!=null && latLng_meta!=null){
                    dato=angulo(latLng_previo.latitude,latLng_previo.longitude,latLng_meta.latitude,latLng_meta.longitude,latLng.latitude,latLng.longitude);

                    Log.i("Log", "calculando rotacion: "+ dato);
                    SharedPreferences myprefs= getSharedPreferences("user", MODE_WORLD_READABLE);
                    myprefs.edit().putString("session_id", dato.intValue()+"").commit();

                }
                else{Log.i("Log", "no calculando rotacion!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");}
                latLng_previo = latLng;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        public Double angulo(Double p11,Double p12, Double p21,Double p22,Double p31,Double p32)
        {
            Double m2 = ( p12 - p22)/(p11-p21);

            Double m1 = (p32-p22)/( p31 - p21)  ;
            Double tan =(m1-m2)/(1+m1*m2);
            Double angulo = Math.atan(tan);
            angulo = angulo*180/Math.PI;
            Double angulo2 = 180-angulo;
            return angulo2;
        }
        public Double getDato(){return dato;}
    }


    private Runnable mUpdate = new Runnable() {
        public void run() {

            longitud0.setText(valores[1]);

            mHandler.postDelayed(this, 1000);
            longitud0.invalidate();
        }
    };
    //LatLng cusco = new LatLng(-13.525,-71.9722);
    public void debugMsg() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latitud0.setText(valores[0]);
                longitud0.setText(valores[1]);
                latLng = new LatLng(Double.parseDouble( valores[0]),Double.parseDouble( valores[1]));
                Putin = mMap.addMarker(new MarkerOptions().position(latLng).title("Carrito!!!"));
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        latLng, 15);
                mMap.animateCamera(location);
                //calcular angulo de rotacion


            }

        });
    }

}
