package com.swordrunner.swordrunner.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.GameRes;
import com.swordrunner.swordrunner.api.service.MapRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.GenericResponse;
import com.swordrunner.swordrunner.data.model.Map;
import com.swordrunner.swordrunner.data.model.TimeGeoPoint;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.data.model.UserGame;
import com.swordrunner.swordrunner.databinding.ActivityMapBinding;
import com.swordrunner.swordrunner.ui.profile.MyGamelistActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private final int REQUEST_CHECK_SETTINGS = 2;

    private MapView map = null;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ActivityMapBinding binding;

    private boolean isRecording = false;
    private Polyline polyline;
    private final ArrayList<TimeGeoPoint> pathPoints = new ArrayList<>();
    private Map currentRun = new Map();
    private WebSocket webSocket;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;

    private double currentDistance = 0.0;
    private int currentDays = 0;
    private double targetDistance = 1000000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if (isRecording) {
                stopLocationUpdates();
                sendMapCreate(false);
            }
            initGameList();
            Intent intent = new Intent(MapActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // init user values
        currentDistance = getIntent().getDoubleExtra("distance_today", currentDistance);
        currentDays = getIntent().getIntExtra("days", currentDays);
        targetDistance = getIntent().getDoubleExtra("target_distance", targetDistance);
        int coins = getIntent().getIntExtra("coins", 0);
        binding.mapTextDays.setText("" + currentDays);
        binding.mapTextCoins.setText("" + coins);
        initiateSocketConnection();
        updateProgress();

        binding.mapImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopLocationUpdates();
                    sendMapCreate(false);
                }
                initGameList();
                Intent intent = new Intent(MapActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        map = binding.map;
        map.setTileSource(TileSourceFactory.MAPNIK);

        colorFilter();

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // required in order to show the map
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        });

        IMapController mapController = map.getController();
        // set settings for osmdroid map
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(21.0);
        map.setMinZoomLevel(18.0);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapController.setZoom(19.0); // there is a bug in the emulator where the tiles won't load with zoom 20. if it also happens with other values try to set a different one.

        GeoPoint startPoint = new GeoPoint(49.876245969437385, 8.652609184560045); // Herrngarten
        mapController.setCenter(startPoint);


        polyline = new Polyline();
        polyline.setColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
        polyline.setWidth(10);
        map.getOverlays().add(polyline);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("MapActivity", "Error when updating location");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d("MapActivity", location.toString());
                    GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mapController.setCenter(point);
                    updatePath(point);
                    updateProgress();
                }
            }
        };


        createLocationRequest();

        binding.mapButtonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    Log.d("MapA", "Stop");
                    binding.mapButtonRun.setImageResource(R.drawable.ic_start);
                    stopLocationUpdates();
                    sendMapCreate(true);
                } else {
                    Log.d("MapA", "Start");
                    binding.mapButtonRun.setImageResource(R.drawable.ic_stop);
                    startLocationUpdates();
                }
            }
        });

        binding.mapImageSafehouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safehouse();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }
    private void initiateSocketConnection(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }
    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            super.onOpen(webSocket, response);
            runOnUiThread(()->{ });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
        }
    }

    /**
     * update progress bar with current distance.
     */
    private void updateProgress() {

        if (pathPoints.size() >= 2) {
            currentDistance += calcDistanceBetweenPoints(
                    pathPoints.get(pathPoints.size() - 2),  pathPoints.get(pathPoints.size() - 1)
            );
        }

        binding.progressBar.setProgress((int) ((currentDistance / targetDistance) * 100));
    }

    /**
     * calculates the distance between two points in order to display in progress bar. the real measurement is done on the server.
     * @param p1 point 1
     * @param p2 point 1
     * @return distance in metres
     */
    private double calcDistanceBetweenPoints(GeoPoint p1, GeoPoint p2) {
        double lat1 = p1.getLatitude();
        double lat2 = p2.getLatitude();
        double lon1 = p1.getLongitude();
        double lon2 = p2.getLongitude();
    
        double R = 6371e3;
        double phi1 = lat1 * Math.PI / 180;
        double phi2 = lat2 * Math.PI / 180;
        double deltaPhi = (lat2 - lat1) * Math.PI / 180;
        double deltaL = (lon2 - lon1) * Math.PI / 180;
    
        double a = Math.sin(deltaPhi/2)*Math.sin(deltaPhi/2)+Math.cos(phi1)*Math.cos(phi2)*Math.sin(deltaL/2)
                    *Math.sin(deltaL/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    /**
     * add current location to current run.
     * @param p geo point
     */
    private void updatePath(GeoPoint p) {
        pathPoints.add(new TimeGeoPoint(p, System.currentTimeMillis() / 1000L));

        ArrayList<GeoPoint> gp = new ArrayList<>();
        for (TimeGeoPoint tgp : pathPoints) {
            gp.add(new GeoPoint(tgp.getLatitude(), tgp.getLongitude()));
        }
        polyline.setPoints(gp);
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * apply color filter  to map tiles so that they appear grey.
     */
    private void colorFilter() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        map.getOverlayManager().getTilesOverlay().setColorFilter(filter);
    }


    /**
     * init gps
     */
    protected void createLocationRequest() {

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Log.d("MapActivity", "Location task success");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                        Log.e("MapActivity", "Location task failed");
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.e("MapActivity", sendEx.getMessage());
                    }
                }
            }
        });
    }

    /**
     * run tracking the gps updates
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        isRecording = true;
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        currentRun.startTime = new Date();
    }

    /**
     * stop tracking gps updates
     */
    private void stopLocationUpdates() {
        currentRun.endTime = new Date();
        isRecording = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);

        if (pathPoints.size() > 0) {
            currentRun.startLocation = pathPoints.get(0);
            currentRun.endLocation = pathPoints.get(pathPoints.size() - 1);
            currentRun.route = pathPoints;
        }
    }

    /**
     * save run (send to server)
     */
    private void sendMapCreate(boolean showDialog) {
        Call<GenericResponse> call = Client.get(this).create(MapRes.class).create(currentRun);
        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.code() != 200) {
                    //Snackbar.make(findViewById(android.R.id.content), "Run could not be saved", Snackbar.LENGTH_LONG).show();
                } else {
                    if (response.body() != null) {
                        if (showDialog) finishRun(response.body().message);
                        binding.mapTextCoins.setText((getIntent().getIntExtra("coins", 0) + response.body().value) + "");
                        pathPoints.clear();
                    }
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                //Snackbar.make(findViewById(android.R.id.content), "Something went wrong", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * show success dialog with run info
     * @param message to display
     */
    private void finishRun(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss());
        builder.setMessage(message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * initiate safehouse for one day and pay on server.
     */
    private void safehouse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                int coins = Integer.parseInt(binding.mapTextCoins.getText().toString()) - 500;
                if (coins < 0) {
                    Snackbar.make(findViewById(android.R.id.content), "Not enough coins!", Snackbar.LENGTH_SHORT).show();
                } else {

                    Call<Void> call = Client.get(MapActivity.this).create(UserRes.class).paySafehouse();
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.code() != 200) {
                                Snackbar.make(findViewById(android.R.id.content), "Not enough coins!", Snackbar.LENGTH_LONG).show();
                            } else {
                                binding.mapTextCoins.setText(coins + "");
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Snackbar.make(findViewById(android.R.id.content), "Not enough coins!", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setMessage("Spend 500 coins to go to the safe house today and skip your daily goal. This will only count for today.");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * update game list with new distances
     */
    private void initGameList(){
        getData(getUserId(), new MyGamelistActivity.ListUserInfoCallback() {
            @Override
            public void onSuccess(User user) {
                int length = 0;
                LinkedList<UserGame> myUserGame = user.getGames();
                for(int i = 0; i < myUserGame.size(); i++){
                    String gameId = null;
                    gameId = myUserGame.get(i).getGameId();
                    getGame(gameId,length,i, new MyGamelistActivity.ListGetGameCallback() {
                        @Override
                        public void onSuccess(Game game, int length, int i) {
                            if(game.participants.size() > 1){
                                for(int n = 0; n < game.participants.size(); n++){
                                    if(!game.participants.get(n).equals(getUserId())){
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("userGameId",game.participants.get(n));
                                            jsonObject.put("friendGameId",game.participants.get(n));
                                            jsonObject.put("deleteGame",false);
                                            jsonObject.put("stopRun",true);
                                            webSocket.send(jsonObject.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * get user data
     * @param id of user
     * @param userInfoCallback propagate
     */
    private void getData(String id, MyGamelistActivity.ListUserInfoCallback userInfoCallback) {
        Call<User> call = Client.get(getApplicationContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                userInfoCallback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }

    /**
     * get specific game data
     * @param GameId
     * @param length
     * @param i
     * @param getGameCallback
     */
    private void getGame(String GameId,int length, int i, MyGamelistActivity.ListGetGameCallback getGameCallback){
        Game thisGame = new Game();
        thisGame.setId(GameId);
        Call<Game> call = Client.get(getApplicationContext(),false).create(GameRes.class).searchGame(thisGame);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if(response.code() == 200)
                    getGameCallback.onSuccess(response.body(),length,i);
            }

            @Override
            public void onFailure(Call<Game> call, Throwable t) {
                getGameCallback.onFailure();
            }
        });
    }

    /**
     * get user id from session
     * @return user id
     */
    private String getUserId(){
        SharedPreferences pref = getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentId = pref.getString("id","").toString();
        return currentId;
    }

}