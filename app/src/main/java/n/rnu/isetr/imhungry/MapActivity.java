package n.rnu.isetr.imhungry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
 import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
 import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.maps.*;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MapActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {

    private MapView mapView;
    double currentLat = 0, currentLong = 0;
    ImageView IVFind;
    private MapElementLayer mPinLayer;
    String selectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Hide action bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        // Retrieve the saved language from shared preferences
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        // Set the locale based on the saved language
        setLocale(language);

        String[] foodCategories = getResources().getStringArray(R.array.food_categories);

        Spinner spin = findViewById(R.id.categorySP);
        spin.setOnItemSelectedListener(this);

        // Create the instance of ArrayAdapter
        // having the list of food categories
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, R.layout.spinner_items, foodCategories);


        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spin.setAdapter(ad);

        IVFind=findViewById(R.id.find);



        RippleBackground rippleBackground = findViewById(R.id.ripple_bg);

        mapView = new MapView(this, MapRenderMode.VECTOR);  // or use MapRenderMode.RASTER for 2D map
        mapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);
        ((FrameLayout)findViewById(R.id.map_view)).addView(mapView);
        mapView.onCreate(savedInstanceState);

        mPinLayer = new MapElementLayer();
        mapView.getLayers().add(mPinLayer);



        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            // Permission has already been granted
            showCurrentLocation();
        }

        IVFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rippleBackground.startRippleAnimation();


                String searchEndpoint = "https://api.geoapify.com/v2/places?apiKey=f17c003ffc414155818cf3020279e1e2" +
                        "&categories="+ selectedItem+
                        "&limit=5" +
                        "&lat=" + currentLat +
                        "&lon=" + currentLong;



                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest searchRequest = new StringRequest(Request.Method.GET, searchEndpoint, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject json = new JSONObject(response);
                            JSONArray features = json.getJSONArray("features");

                            // Loop through the features
                            for (int i = 0; i < features.length(); i++) {
                                JSONObject feature = features.getJSONObject(i);

                                // Extract the location coordinates and name
                                JSONObject geometry = feature.getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double latitude = coordinates.getDouble(1);
                                double longitude = coordinates.getDouble(0);
                                String placeName = feature.getJSONObject("properties").getString("name");

                                MapIcon pushpin = new MapIcon();
                                pushpin.setLocation(new Geopoint(latitude, longitude));
                                pushpin.setTitle(placeName);
                                mPinLayer.getElements().add(pushpin);
                            }
                            // Stop the ripple animation
                            rippleBackground.stopRippleAnimation();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error retrieving search results", Toast.LENGTH_SHORT).show();
                    }
                });

                queue.add(searchRequest);

            }
        });

    }

    private void setLocale(String lang){
        Locale locale=new Locale(lang);
        Locale.setDefault(locale);
        Configuration config=new Configuration();
        config.locale=locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        //save data to shared preferences
        SharedPreferences.Editor editor=getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",lang);
        editor.apply();

    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                showCurrentLocation();
            } else {
                // Permission denied
            }
        }
    }


    private void showCurrentLocation() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if location services are enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            // Get the last known location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                currentLat = lastKnownLocation.getLatitude();
                currentLong = lastKnownLocation.getLongitude();

                // Create a marker for the current location
                MapIcon pushpin = new MapIcon();
                Geopoint mypos=new Geopoint(currentLat,currentLong);
                pushpin.setLocation(mypos);
                pushpin.setTitle("Current Location");

                // Add the map icon to the pin layer
                mPinLayer.getElements().add(pushpin);

                // Center the map on the current location and zoom to it
                 mapView.setScene(
                        MapScene.createFromLocationAndZoomLevel(mypos, 15),
                        MapAnimationKind.NONE);
            } else {
                // No last known location found
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Location services are not enabled
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(position==0){
            selectedItem="catering.fast_food";
        }
        else{
            selectedItem="commercial.food_and_drink";

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
