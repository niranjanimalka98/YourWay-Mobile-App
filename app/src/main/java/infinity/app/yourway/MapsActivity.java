package infinity.app.yourway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import infinity.app.yourway.Model.BusModel;
import infinity.app.yourway.Model.LoggedUser;
import infinity.app.yourway.Model.PassengerModel;
import infinity.app.yourway.activity.ComplainActivity;
import infinity.app.yourway.activity.LoginActivity;
import infinity.app.yourway.activity.RegisterActivity;
import infinity.app.yourway.databinding.ActivityMapsBinding;
import infinity.app.yourway.tools.Utils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private double lat;
    private double lng;
    double distance_between_user_and_bus;

    LatLng passenger_location = new LatLng(lat, lng);
    private LocationRequest locationRequest;

    String url;
    RequestQueue queue;

    String url_search;
    RequestQueue queue_search;

    String url_search3;
    RequestQueue queue_search3;
    JsonArrayRequest jsonArrayRequest;
    JsonArrayRequest jsonArrayRequest2;
    JsonArrayRequest jsonArrayRequest3;
    DatabaseReference db;
    BusModel bus = new BusModel();

    ImageView search;
    EditText searchtext;
    CardView bus_info_card;
    ImageView close;
    TextView bus_number;
    TextView estimate_time;
    TextView route;
    TextView distance;
    TextView destination;
    Button complain;

    String bus_id;

    RequestQueue myrequestQueue;
    JsonObjectRequest myjsonArrayRequest;
    String myurl;

    Button button;
    PassengerModel loggedInUser;

    ProgressBar progressBar;
    CardView cardView;



    private final static int REQUEST_CODE=100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loggedInUser = Utils.get_logged_in_user();
        if(loggedInUser == null){
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LoginActivity.class);
            this.startActivity(i);
            finish();
            return;
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LoggedUser loggedUser = new LoggedUser();
        loggedUser.passenger = loggedInUser;
        Log.d("UserID", loggedUser.passenger.getNic());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        bus_info_card = findViewById(R.id.bus_info_card);
        close = findViewById(R.id.close);
        bus_number = findViewById(R.id.bus_number);
        estimate_time = findViewById(R.id.estimate_time);
        route = findViewById(R.id.route);
        distance = findViewById(R.id.distance);
        destination = findViewById(R.id.destination);
        complain = findViewById(R.id.btn_complain);
        //progress bar and card view
        progressBar = findViewById(R.id.progressBar);
        cardView = findViewById(R.id.card);
        cardView.setVisibility(View.GONE);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, RegisterActivity.class);
                MapsActivity.this.startActivity(intent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus_info_card.setVisibility(View.GONE);
            }
        });
        getCurrentLocation();
        get_searched_locations();
        send_complain();

    }

    private void send_complain() {

        complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggedUser loggedUser = new LoggedUser();
                loggedUser.passenger = loggedInUser;
                Log.d("UserID", loggedUser.passenger.getNic());
                Intent i = new Intent(MapsActivity.this, ComplainActivity.class);
                i.putExtra("bus_id", bus_id);
                i.putExtra("user_id", loggedUser.passenger.getNic());
                MapsActivity.this.startActivity(i);
                //Toast.makeText(MapsActivity.this, "You clicked at: "+bus_id, Toast.LENGTH_SHORT).show();

            }
        });
    }

    //this segment check check whether mobile device gps permissions granted or not. if permission already granted, it gets current user location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    //this segment get gps coordinates
    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        lat = latitude;
                                        lng = longitude;

                                        Toast.makeText(MapsActivity.this, "Latitude: "+latitude, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(MapsActivity.this, "Longitude: "+longitude, Toast.LENGTH_SHORT).show();

                                        LatLng LK = new LatLng(latitude, longitude);
                                        mMap.addMarker(
                                                new MarkerOptions()
                                                        .position(LK)
                                                        .title("Marker in LK")
                                                        .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.location)));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LK, 15));
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //if gps turned off, then send message to user
    private void turnOnGPS() {



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MapsActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MapsActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

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
    //set google maps default marker
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng LK = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(LK).title("Marker In Sri Lanka"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LK));

    }


    //this segment call Spring boot API and get available bus results according to the search string
    private void get_searched_locations() {

        db = FirebaseDatabase.getInstance().getReference();

        search = findViewById(R.id.search_btn);
        searchtext = findViewById(R.id.search_text);


        url = "https://infinity-bus-app.herokuapp.com/api/filterBus?user_destination=habarana";

        queue = Volley.newRequestQueue(this);



        queue_search = Volley.newRequestQueue(this);
        queue_search3 = Volley.newRequestQueue(this);

        myrequestQueue = Volley.newRequestQueue(this);


        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cardView.setVisibility(View.GONE);
                int i;
                for(i=0; i<response.length(); i++){
                    try {
                        JSONObject jsn = response.getJSONObject(i);
                        int j = i;
                        String busNO = jsn.getString("bus_no");


                        //Toast.makeText(MapsActivity.this, busNO, Toast.LENGTH_SHORT).show();

                        //start realtime data tracking
                        final ArrayList<String> list = new ArrayList<>();

                        db.child("buses").child(" "+busNO).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("PotentialBehaviorOverride")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.getValue() ==null){
                                    bus.lat = "null";
                                    bus.lng = "null";
                                }

                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                                    list.clear();



                                    try {
                                        list.add(response.getJSONObject(j).toString());
                                        list.add(snapshot.getValue().toString());
                                        bus = snapshot.getValue(BusModel.class);




                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }

                                if (bus.lat == "null"){
                                    System.out.println("njnout: coordinates are empty");
                                }else{

                                    double latitude = Double.parseDouble(bus.lat);
                                    double longitude = Double.parseDouble(bus.lng);
                                    LatLng LK = new LatLng(latitude, longitude);
                                    mMap.addMarker(
                                            new MarkerOptions()
                                                    .position(LK)
                                                    .title(busNO)
                                                    .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.icl_bus)));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LK, 15));

                                    //System.out.println("njnout "+bus.lat + ", " + bus.lng);
                                }
                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(@NonNull Marker marker) {

                                        cardView.setVisibility(View.VISIBLE);
                                        url_search = "https://demo-njn.herokuapp.com/searchBus?bus_no="+marker.getTitle();
                                        bus_id = marker.getTitle();

                                        jsonArrayRequest2 = new JsonArrayRequest(Request.Method.GET, url_search, null, new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                cardView.setVisibility(View.GONE);
                                                try {
                                                    JSONObject bus_info = response.getJSONObject(0);
                                                    String s_bus_number = bus_info.getString("bus_no");
                                                    String s_bus_route = bus_info.getString("route");
                                                    String s_bus_destination = bus_info.getString("destination");

                                                    bus_number.setText(s_bus_number);
                                                    route.setText("Route: "+s_bus_route);
                                                    destination.setText(s_bus_destination);
                                                    db.child("buses").child(" "+s_bus_number).addValueEventListener(new ValueEventListener() {
                                                                                                                        @Override
                                                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                                            bus = snapshot.getValue(BusModel.class);

                                                                                                                            myurl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+lat+","+lng+"&destinations="+bus.lat+","+bus.lng+"&key=AIzaSyAVSbwauyfVApyx8JMdZaYa6uGfPDsNPEA";


                                                                                                                            myjsonArrayRequest = new JsonObjectRequest(Request.Method.GET, myurl, null, new Response.Listener<JSONObject>() {
                                                                                                                                @Override
                                                                                                                                public void onResponse(JSONObject response) {
                                                                                                                                    try {
                                                                                                                                        JSONObject jsonObject = new JSONObject(String.valueOf(response));
                                                                                                                                        JSONArray rows = jsonObject.getJSONArray("rows");
                                                                                                                                        JSONObject col = rows.getJSONObject(0);
                                                                                                                                        JSONArray col2 = col.getJSONArray("elements");
                                                                                                                                        JSONObject col3 = col2.getJSONObject(0);
                                                                                                                                        JSONObject API_distance = col3.getJSONObject("distance");
                                                                                                                                        JSONObject API_duration = col3.getJSONObject("duration");
                                                                                                                                        String M_distance = API_distance.getString("text") ;
                                                                                                                                        String M_duration = API_duration.getString("text") ;
                                                                                                                                        distance.setText(M_distance);
                                                                                                                                        estimate_time.setText(M_duration);
                                                                                                                                        Log.d("NJN", M_distance);
                                                                                                                                        Log.d("NJN", M_duration);
                                                                                                                                    } catch (JSONException e) {
                                                                                                                                        e.printStackTrace();
                                                                                                                                    }


                                                                                                                                }
                                                                                                                            }, new Response.ErrorListener() {
                                                                                                                                @Override
                                                                                                                                public void onErrorResponse(VolleyError error) {
                                                                                                                                    Toast.makeText(MapsActivity.this, "failed"+error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                    Log.d("NJN", error.getMessage());
                                                                                                                                }
                                                                                                                            });
                                                                                                                            queue.add(myjsonArrayRequest);

                                                                                                                            //https://maps.googleapis.com/maps/api/distancematrix/json?origins=7.996584,80.938014&destinations=8.002034,80.924301&key=AIzaSyAVSbwauyfVApyx8JMdZaYa6uGfPDsNPEA
//                                                                                                                            LatLng passenger_location = new LatLng(lat, lng);
//                                                                                                                            LatLng bus_location = new LatLng(Double.valueOf(bus.lat), Double.valueOf(bus.lng));
//
//                                                                                                                            double distance_between_passenger_and_bus = SphericalUtil.computeDistanceBetween(passenger_location, bus_location);
//                                                                                                                            distance.setText("Distance: "+String.format("%.2f", distance_between_passenger_and_bus / 1000)+"Km");

//

                                                                                                                        }

                                                                                                                        @Override
                                                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                                                        }
                                                                                                                    });
                                                    bus_info_card.setVisibility(View.VISIBLE);
                                                    //Log.d("TEST", String.valueOf(bus_info));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                cardView.setVisibility(View.GONE);
                                            }
                                        });
                                        queue_search.add(jsonArrayRequest2);

                                        return true;
                                    }
                                });
                                //Log.d("TEST", snapshot.getValue().toString());
                                //Log.d("TEST", list.toString());

                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //end realtime data tracking

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                //textView.setText(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cardView.setVisibility(View.GONE);
                //textView.setText(error.toString());
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.VISIBLE);
                queue.add(jsonArrayRequest);

            }
        });



    }

    LinearLayout details;
    LinearLayout layout;
    public void expand(View view) {
        details = findViewById(R.id.details);
        layout = findViewById(R.id.layout);
        layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        int v = (details.getVisibility() == View.GONE)? View.VISIBLE: View.GONE;
        TransitionManager.beginDelayedTransition(layout, new AutoTransition());
        details.setVisibility(v);
    }




    public void log_out(View view) {
        try {
            PassengerModel.deleteAll(PassengerModel.class);
            Toast.makeText(this, "Logged you out successfully!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MapsActivity.this, MapsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            MapsActivity.this.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to Log you out because " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}