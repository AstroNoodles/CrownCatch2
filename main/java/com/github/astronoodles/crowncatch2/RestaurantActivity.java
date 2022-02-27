package com.github.astronoodles.crowncatch2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.astronoodles.crowncatch2.cards.RestaurantAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {

    public FusedLocationProviderClient locationClient;
    public MapView mapView;
    public RestaurantAdapter restAdapter;
    public TextView emptyResults;

    private final List<String> restNames = new ArrayList<>(20);
    private final List<Float> restRatings = new ArrayList<Float>(20);
    private final List<Float> restMetersAway = new ArrayList<>(20);
    private final List<Boolean> restTakeOutAvailable = new ArrayList<>(20);
    private final List<Boolean> restDineInAvailable = new ArrayList<>(20);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_activity);

        emptyResults = findViewById(R.id.emptyRecyclerText);

        mapView = findViewById(R.id.restaurant_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(10.0);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        RecyclerView placeNames = findViewById(R.id.recycler_restaurants);

        placeNames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        restAdapter = new RestaurantAdapter(restNames, restRatings, restMetersAway,
                restTakeOutAvailable, restDineInAvailable);
        placeNames.setAdapter(restAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(placeNames.getContext(), DividerItemDecoration.VERTICAL);
        placeNames.addItemDecoration(divider);


        MapActivity2.getPermissionAccess(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET});

        updateNearbyPlaces();

        MapActivity2.createNotificationChannel(this);
        NotificationManagerCompat nmc = NotificationManagerCompat.from(this);

        nmc.notify(4, MapActivity2.buildPointsNotification(this, getString(R.string.finding_restaurants)).build());
        MapActivity2.addPointsToCounter(this, 10);
    }

    private String getNearbyPlaceRequest(double latitude, double longitude){
        final int restRadius = 1600; // 1 mile

        StringBuilder placeURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        placeURL.append(String.format("&key=%s", getString(R.string.ipa_key)));
        placeURL.append(String.format("&radius=%d", restRadius));
        placeURL.append(String.format("&location=%f,%f", latitude, longitude));
        placeURL.append(String.format("&types=%s|%s", "restaurant", "meal_takeaway"));

        return placeURL.toString();
    }


    private void updateNearbyPlaces(){
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                String placeURL = getNearbyPlaceRequest(location.getLatitude(), location.getLongitude());
                mapView.getController().setCenter(new GeoPoint(location));
                new PlaceAsyncTask(location).execute(placeURL);

            }
        });

        locationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(RestaurantActivity.this, "Something went wrong! Did you enable your location services?",
                        Toast.LENGTH_SHORT).show();
            }
        });
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            MapActivity2.handlePermissions(this, permissions, grantResults);
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
            //    // ASYNC TASK FOR GETTING JSON RESULTS FROM PLACES API    //      //
           //    // ---------------------------------------------------    //      //

    public class PlaceAsyncTask extends AsyncTask<String, Void, JSONObject> {

        private final Location currLoc;

        private PlaceAsyncTask(Location currLoc){
            this.currLoc = currLoc;
        }


        @Override
        protected JSONObject doInBackground(String... strings) {
            String placeString = strings[0];


            try {
                URL placeURL = new URL(placeString);
                URLConnection connex = placeURL.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connex.getInputStream()));
                String inputLine;
                StringBuilder jsonReserve = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    jsonReserve.append(inputLine).append(System.lineSeparator());
                }
                return new JSONObject(jsonReserve.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            final List<Double> placeLatitudes = new ArrayList<>(20);
            final List<Double> placeLongitudes = new ArrayList<>(20);

            try {
                String status = jsonObject.getString("status");

                if(status.equals("OK")) {
                    emptyResults.setVisibility(View.GONE);

                    JSONArray queryResults = jsonObject.getJSONArray("results");

                    for(int i = 0; i < queryResults.length(); i++) {

                        double latitude = queryResults.getJSONObject(i).getJSONObject("geometry").
                                getJSONObject("location").getDouble("lat");
                        float rating = (float) queryResults.getJSONObject(i).getDouble("rating");
                        double longitude = queryResults.getJSONObject(i).getJSONObject("geometry").
                                getJSONObject("location").getDouble("lng");
                        String locName = queryResults.getJSONObject(i).getString("name");

                        String restType = queryResults.getJSONObject(i).getJSONArray("types").getString(0);

                        if(queryResults.getJSONObject(i).has("opening_hours")) {
                            boolean locTakeOut = queryResults.getJSONObject(i).getJSONObject("opening_hours").
                                    getBoolean("open_now") && (restType.equals("meal_takeaway"));
                            boolean canDineIn = (queryResults.getJSONObject(i).getJSONArray("types").getString(0)
                                    .equals("restaurant"))
                                    && queryResults.getJSONObject(i).getJSONObject("opening_hours").getBoolean("open_now");

                            restTakeOutAvailable.add(locTakeOut);
                            restDineInAvailable.add(canDineIn);
                        } else {
                            restTakeOutAvailable.add(false);
                            restDineInAvailable.add(false);
                        }
                        placeLatitudes.add(latitude);
                        placeLongitudes.add(longitude);

                        restRatings.add(rating);
                        restNames.add(locName);
                    }

                    updateMapMarkers(currLoc, placeLatitudes, placeLongitudes);
                    restAdapter.notifyDataSetChanged();
                    System.out.println("finished!");
                } else if (status.equals("ZERO_RESULTS")) {
                    emptyResults.setText(R.string.text_zero_results);
                } else {
                    emptyResults.setText(R.string.no_loc_found);
                    System.err.println(jsonObject.getString("error_message"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void updateMapMarkers(final Location currLoc, final List<Double> placeLatitudes,
                                      final List<Double> placeLongitudes) {
            Location toLoc = new Location("");

            // Use a trick! Since longitudes MUST be same size as latitudes per location, can access with same index
            for(int i = 0; i < placeLatitudes.size(); i++){
                toLoc.setLongitude(placeLongitudes.get(i));
                toLoc.setLatitude(placeLatitudes.get(i));

                Marker placeMarker = new Marker(mapView);
                placeMarker.setTextLabelFontSize(16);
                placeMarker.setTextLabelBackgroundColor(Color.WHITE);
                placeMarker.setTextIcon(restNames.get(i));
                placeMarker.setPosition(new GeoPoint(toLoc));
                placeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                mapView.getOverlays().add(placeMarker);


                restMetersAway.add(currLoc.distanceTo(toLoc));

            }
        }

    }

}
