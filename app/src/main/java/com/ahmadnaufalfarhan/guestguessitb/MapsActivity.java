package com.ahmadnaufalfarhan.guestguessitb;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    // compass attributes
    private ImageView imageCompass;
    private float currentDegree = 0f;
    float[] mAccelerometer;
    float[] mMagnetometer;

    // sensor handler for compass
    private SensorManager sensorManager;
    Sensor accelerometerSensor;
    Sensor magnetometerSensor;

    private GoogleMap mMap;
    public static LatLng currentPosition;
    public static double latitude;
    public static double longitude;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageButton buttonCenterMap = (ImageButton) findViewById(R.id.buttonCenterMap);
        buttonCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if clicked, reposition camera to original position (ITB)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17));
            }
        });

        imageCompass = (ImageView) findViewById(R.id.imageCompass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Intent callingIntent = getIntent();
        if (latitude == 0f && longitude == 0f) {
            latitude = callingIntent.getDoubleExtra(Identification.PRM_LATITUDE, 0);
            longitude = callingIntent.getDoubleExtra(Identification.PRM_LONGITUDE, 0);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the extra intent
        currentPosition = new LatLng(latitude, longitude);

        // register the listener
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME);
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
        mMap.addMarker(new MarkerOptions().position(currentPosition).title("Marker in ITB"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17));
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
                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_90:
                        azimuth += 90; break;
                    case Surface.ROTATION_180:
                        azimuth += 180; break;
                    case Surface.ROTATION_270:
                        azimuth -= 90; break;
                    default:
                        break;
                }

                RotateAnimation ra = new RotateAnimation(currentDegree, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(210);

                ra.setFillAfter(true);

                imageCompass.startAnimation(ra);
                currentDegree = -azimuth;
            }
        }
    }

    /**
     *  Get the camera intent
     */
    public void startPictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File pictureFile = null;
            try {
                pictureFile = saveImageFile();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MapsActivity.this, "Error IO Exception", Toast.LENGTH_SHORT).show();
            }

            if (pictureFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Start the Submit Answer Activity
     */
    public void startSubmitAnswerActivity(View view) {
        Intent submitAnswerActivity = new Intent(this, SubmitAnswerActivity.class);
        startActivity(submitAnswerActivity);
    }

    /**
     * Activity to save a photo after a camera activity
     */
    public File saveImageFile() throws IOException {
        // Create the image file
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "GGITB_"+ timestamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Save the image file with the properties
        File imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return imageFile;
    }
}
