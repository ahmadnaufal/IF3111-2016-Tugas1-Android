package com.ahmadnaufalfarhan.guestguessitb;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private ImageView imageCompass;
    private float currentDegree = 0f;
    float[] mAccelerometer;
    float[] mMagnetometer;

    private SensorManager sensorManager;
    Sensor accelerometerSensor;
    Sensor magnetometerSensor;

    private static final double ITB_LATITUDE = -6.89284;
    private static final double ITB_LONGITUDE = 107.61052;

    private GoogleMap mMap;
    private LatLng itb;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton buttonCenterMap = (FloatingActionButton) findViewById(R.id.buttonCenterMap);
        buttonCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if clicked, reposition camera to original position (ITB)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itb, 17));
            }
        });

        imageCompass = (ImageView) findViewById(R.id.imageCompass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register the listener
        boolean isAccel = sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        boolean isMagnet = sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
        Log.i("Kucing", String.valueOf(isAccel) + " " + String.valueOf(isMagnet));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save battery by releasing sensor handles
        sensorManager.unregisterListener(this);
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
        itb = new LatLng(ITB_LATITUDE, ITB_LONGITUDE);
        mMap.addMarker(new MarkerOptions().position(itb).title("Marker in ITB"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itb, 17));
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mAccelerometer = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mMagnetometer = event.values;

        if (mAccelerometer != null && mMagnetometer != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, mAccelerometer, mMagnetometer)) {
                float orientation[] = new float[3];
                float azimuth = (float) (Math.toDegrees( SensorManager.getOrientation(R, orientation)[0]) + 360) % 360;

                RotateAnimation ra = new RotateAnimation(currentDegree, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(210);

                ra.setFillAfter(true);

                imageCompass.startAnimation(ra);
                currentDegree = -azimuth;
            }
        }
    }

    /* Get the camera intent */
    public void startPictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /* Start the Submit Answer Activity */
    public void startSubmitAnswerActivity(View view) {
        Intent submitAnswerActivity = new Intent(this, SubmitAnswerActivity.class);
        startActivity(submitAnswerActivity);
    }
}
