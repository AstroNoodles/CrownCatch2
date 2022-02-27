package com.github.astronoodles.crowncatch2;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity2 extends AppCompatActivity{

    public FusedLocationProviderClient locationProviderClient;
    private EditText address;
    private TextView milesAway;
    private MapView mapView;
    private Handler h;


    public static final String CHANNEL_ID = "crown_notifier_maps";
    public static final String PREFERENCE_NAME = "POINT_PREF";
    public static final String POINT_KEY = "points";
    private static final double TIME_NOTIFICATON = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map2);

        address = findViewById(R.id.address);
        milesAway = findViewById(R.id.miles);
        h = new Handler(Looper.getMainLooper());
        final Context c = this;

        Configuration.getInstance().load(c, PreferenceManager.getDefaultSharedPreferences(c));

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(7.5);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setMultiTouchControls(true);

        getPermissionAccess(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE});

        createNotificationChannel(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            Button locButton = findViewById(R.id.locButton);
            locButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!address.getText().toString().equals("")) {
                        getLocation(mapView.getController());
                    } else {
                        Toast.makeText(c, R.string.fill_location_alert, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crowncatchmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.selectMenuOptions(this, item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public static void createNotificationChannel(Context c){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = c.getSystemService(NotificationManager.class);

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                String channel_name = c.getString(R.string.channel_name);
                String channel_desc = c.getString(R.string.channel_desc);
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channel_name, importance);
                channel.setDescription(channel_desc);

                manager.createNotificationChannel(channel);
            }
        }
    }


    public static NotificationCompat.Builder buildPointsNotification(Context c, String message){

        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, 1, i, 0);

        return new NotificationCompat.Builder(c, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_whatshot_green_24dp)
                .setContentTitle(c.getString(R.string.great_job))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);
    }

    private void sendTimeNotification(final NotificationManagerCompat nmc, final int points, final Location pointA, final Location pointB){
        double toEnd = 3;
        System.out.println("hello!");
        for(double counter = TIME_NOTIFICATON; counter < toEnd; counter += TIME_NOTIFICATON){
            long milliCounter = 60 * 1000; // in actuality, this should be 1 hour but 1 min is quick!
            System.out.println("testing millicounter " + milliCounter);
            final String message = getString(R.string.stay_home_message, points);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("inside the handler");
                    float distance = pointA.distanceTo(pointB);
                    if(distance <= 1000){
                        nmc.notify(1, buildPointsNotification(MapActivity2.this, message).build());
                    }
                }
            }, milliCounter);
        }
    }

    public static void addPointsToCounter(Context c, int points){
        SharedPreferences prefs = c.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edits = prefs.edit();
        edits.putInt(POINT_KEY, prefs.getInt(POINT_KEY, 0) + points);

        System.out.printf("CURRENT POINTS IN PROFILE: %d", prefs.getInt(POINT_KEY, 0));

        edits.commit();
    }


    private void getLocation(final IMapController controller){
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                System.out.println("herl");
                if(location != null){
                    Location location1 = new Location("");
                    System.out.println(location.getLatitude());
                    GeoPoint mapPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    controller.setCenter(mapPoint);


                    float[] latlong = getLatLongFromPlaceName(address.getText().toString());
                    Location myloc = new Location("");
                    GeoPoint currLoc = new GeoPoint(latlong[0], latlong[1]);
                    myloc.setLatitude(latlong[0]);
                    myloc.setLongitude(latlong[1]);
                    System.out.println("m" + latlong[0]);
                    System.out.println("m" + latlong[1]);

                    float distance = location.distanceTo(myloc);
                    BoundingBox markerBox = BoundingBox.fromGeoPoints(Arrays.asList(mapPoint, currLoc));
                    mapView.zoomToBoundingBox(markerBox, true);

                    setMarkerAndLines(mapPoint, currLoc, distance);
                    milesAway.setText(getString(R.string.miles_away, (distance / 1609)));
                    NotificationManagerCompat nmc = NotificationManagerCompat.from(getBaseContext());

                    // Distance check in meters! 1 km is no good...
                    if(distance >= 1000) {
                        String message = getString(R.string.precaution_message, (distance / 1609));
                        nmc.notify(1, buildDistanceNotification(message).build());
                    } else {
                        SharedPreferences prefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putInt(POINT_KEY, prefs.getInt(POINT_KEY, 0) + 20);
                        edit.commit();
                        sendTimeNotification(nmc, prefs.getInt(POINT_KEY, 0), location, myloc);
                        System.out.println("CURRENT POINTS VALUE: " + prefs.getInt(POINT_KEY, 0));
                    }
                }

            }
        });

        locationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("What happened?");
                e.printStackTrace();
            }
        });
    }

    private NotificationCompat.Builder buildDistanceNotification(String message) {

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 1, i, 0);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle(getString(R.string.stay_home_alert))
                .setSmallIcon(R.drawable.ic_error_outline_red_24dp)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);
    }

    private void setMarkerAndLines(GeoPoint lastLoc, GeoPoint currLoc, final float distance){
        Marker lastLocMark = new Marker(mapView);
        Marker currPos = new Marker(mapView);

        lastLocMark.setPosition(lastLoc);
        currPos.setPosition(currLoc);
        lastLocMark.setTextLabelFontSize(40);
        currPos.setTextLabelFontSize(40);
        lastLocMark.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        currPos.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        mapView.getOverlays().add(lastLocMark);
        mapView.getOverlays().add(currPos);


        List<GeoPoint> geoPoints = Arrays.asList(lastLoc, currLoc);
        Polyline line = new Polyline(mapView);
        line.setPoints(geoPoints);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), String.format("%f miles from Home!", (distance / 1609)), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        mapView.getOverlayManager().add(line);
    }

    private float[] getLatLongFromPlaceName(String name){
        Geocoder coder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;
        try {
            addressList = coder.getFromLocationName(name, 5);
            Address a = addressList.get(0);
            System.out.println(a.getLatitude());
            System.out.println(a.getLongitude());
            return new float[]{(float)(a.getLatitude()), (float) (a.getLongitude())};

        } catch(IOException e){
            Toast.makeText(this, R.string.loc_error, Toast.LENGTH_LONG).show();
        }
        return new float[]{0, 0};
    }

    public static void getPermissionAccess(Activity activity, String[] manifestPermissions){
        ArrayList<String> permissionsToGet = new ArrayList<>();
        for(String permission : manifestPermissions){
            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
                permissionsToGet.add(permission);
            }
        }

        if(permissionsToGet.size() > 0){
            ActivityCompat.requestPermissions(activity, permissionsToGet.toArray(new String[0]), 1);
        }
    }

    public static void handlePermissions(AppCompatActivity activity, @NonNull String[] permissions, @NonNull int[] grantResults){
        ArrayList<String> permissionsToAsk = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));

        if(permissionsToAsk.size() > 1) {
            ActivityCompat.requestPermissions(activity, permissionsToAsk.toArray(new String[0]), 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      if(requestCode == 1){
          handlePermissions(this, permissions, grantResults);
      } else {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      }
    }
}
