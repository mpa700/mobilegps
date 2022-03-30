package com.ecttion.pilotoboradevan;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.squareup.okhttp.OkHttpClient;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permissions;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static android.os.Build.VERSION.SDK_INT;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
@SuppressLint("Registered")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    public GoogleMap mMap;
    public static String os = "";
    public static final String versao = "Android 10 API";
    public static final String car = "";
    public static String mot = "";
    public static final String servidor = "https://boradevan.com/interface2/jcd.php?s=sort";
    public static final String servidorx = "https://boradevan.com/interface2/conectcar.php?versao=" + versao +"&s=sort";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 9999;
    public static final String EXTRA_LIGAR_E_DESTRAVAR_TELA = Objects.requireNonNull(MapsActivity.class.getPackage()).getName() + ".LIGAR_E_DESTRAVAR_TELA";
    public static String[] dadoscoords = null;
    public int i = 0;
    public int r = 0;
    public static String flagrota = "0";
    public static LatLng desti = null;
    @SuppressLint("StaticFieldLeak")
    public static TextView mTextView2;
    @SuppressLint("StaticFieldLeak")
    public static TextView mTextView;
    public static LatLng posi = new LatLng(0, 0);
    private Polyline currentPolyline;
    public static double distance = 0;
    public static double gdistance = 0;
    public static String coordenadas = "";
    private static final int PERMISSIONS_REQUEST = 9999;
    public static MediaPlayer mp = null;
    public static String novarota = "y";
    public static Marker mMarker = null;
    public static long intervalo = 1000;
    public static String uidenti = "";
    public static String uisim ;
    @SuppressLint("StaticFieldLeak")
    public static WebView webView;
    public static String ligamonitoring = "";

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt", "Assert", "SetTextI18n", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getIntent().getBooleanExtra(EXTRA_LIGAR_E_DESTRAVAR_TELA, false)) {
            // Combinando duas flags: FLAG_TURN_SCREEN_ON e FLAG_DISMISS_KEYGUARD.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            mapFragment.setMenuVisibility(true);
        }
        if (SDK_INT >= 26) {
            MapsActivity.this.startForegroundService(new Intent(this, TestService.class));
        } else {
            startService(new Intent(getBaseContext(), TestService.class));
        }
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> tasks = null;
            if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
                tasks = am.getAppTasks();
            }
            if (tasks != null && tasks.size() > 0) {
                tasks.get(0).setExcludeFromRecents(true);
            }

        }
        appInitialization();
        checkLocationPermission();
        checkPermissions();
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId, tdNumber;
        assert tm != null;
        tmDevice = tm.getDeviceId();
        tmSerial = tm.getSimSerialNumber();
        androidId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uisim = deviceUuid.toString();
        Log.e("**id**", uisim);


        webView = findViewById(R.id.web1);
        WebSettings webSettings = webView.getSettings();
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);


        webView.setWebChromeClient(new WebChromeClient(){
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl(servidorx + "&assn=" + uisim);
            }



            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!isDeviceOnline(getApplicationContext())) {
                    view.loadUrl("file:///android_asset/noconnection.html?car=" + uisim );
                } else {
                    if (url.contains("https://boradevan.com/interface2/aviso.php?car=")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    if (url.contains("https://boradevan.com/")) {
                        webView.loadUrl(url);
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                }

                return true;

            }
        });

        webView.loadUrl(servidorx + "&assn=" + uisim);
        //mp = MediaPlayer.create(this, R.raw.alert);
        mTextView = findViewById(R.id.textView);
        mTextView2 = findViewById(R.id.textView2);
        assert mTextView != null;
        mTextView.setText(mot);
        mTextView2.setText("");

        if(mTextView.getText() == "") {
            motoristas task0 = new motoristas();
            task0.execute(servidorx + "&car=" + uisim);
        }

        startLocationUpdates();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void startLocationUpdates() {

        //LocationResquest com as definições requeridas
        // 10 seconds, in milliseconds
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
        //Construção dum LocationSettingsRequest com as definições requeridas
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {

        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        mMarker.setPosition(loc);
        if(mMap != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));
            Log.d("teste**", "execute");
            ordemservico task1 = new ordemservico();
            task1.execute(servidorx + "&car=" + uisim);
            getcoordenadas task2 = new getcoordenadas();
            task2.execute(servidorx + "&car=" + uisim);
            if (coordenadas != null && flagrota.equals("1") && !coordenadas.equals("")) {
                Log.d("coordes**", coordenadas);
                novarota = "n";
                executarota(location.getLatitude(), location.getLongitude());
            }

        }
    }

    public void doRestart() {
        Context context = getBaseContext();
        Intent mStartActivity = new Intent(context, MapsActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert mgr != null;
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 240000, mPendingIntent);
        System.exit(0);
    }

    private void appInitialization() {
        //make crash report on ex.stackreport
        Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    public static boolean isDeviceOnline(Context pContext) {
        ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(@NotNull Thread thread, Throwable ex) {
            ex.printStackTrace();
            doRestart();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void checkPermissions() {
        int recriar = 0;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permission1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
            recriar++;
        }else if (permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST);
            recriar++;
        }else if (permission3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
            recriar++;
        }else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST);
            recriar++;

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);

            int hasRecordPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            int hasAudioPermission = checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS);

            List<String> permissions = new ArrayList<>();
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (hasRecordPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]),111);

            }
        }        if(recriar > 0){
            recreate();
        }

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "Assert"})
    private void executarota(double lat, double longi) {
        Log.d("I**", String.valueOf(i));
        Log.d("exe**", coordenadas);
        dadoscoords = coordenadas.split(";");

        if ( i < dadoscoords.length) {
            Log.d("eze**", String.valueOf(dadoscoords.length));
            String[] latLng = dadoscoords[i].split(",");

            if (flagrota.equals("1")) {
                intervalo = 1000;
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);
                desti = new LatLng(latitude, longitude);
                posi = new LatLng(lat, longi);
                MarkerOptions place1 = new MarkerOptions().position(posi).title("Meu local");
                MarkerOptions place2 = new MarkerOptions().position(desti).title("Destino");
                // viagem task3 = new viagem();
                //task3.execute("https://vtaservice.com/mapas/conectcar2.php?versao=" + versao +"&s=sort&car=" + uisim + "&desti=" + desti.latitude + "," + desti.longitude + "&posi=" + posi.latitude + "," + posi.longitude );
                webView.loadUrl(servidorx +"&car=" + uisim + "&desti=" + desti.latitude + "," + desti.longitude + "&posi=" + posi.latitude + "," + posi.longitude + "&os=" + os );
                r++;
                if (r == 20) {
                    novarota = "y";
                    r = 0;
                }
                if (novarota.equals("y")) {
                    new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition()), "DRIVING");
                }

                mMarker.setPosition(place1.getPosition());
                mMap.addMarker(place2);
                distance = SphericalUtil.computeDistanceBetween(posi, desti);

                mTextView2.setText("Distancia: " + String.format("%.2f", arredondar(distance) / 1000) + " km");
                if (gdistance == 0) {
                    gdistance = distance;
                } else if (distance > gdistance) {
                    novarota = "n";
                }
                Log.d("distancia", String.valueOf(distance));
                if (distance <= 300) {
                    i++;

                }
            }
        }else{
            flagrota = "0";
            encerrarota();
        }
    }

    double arredondar(double valor) {
        double arredondado = valor;
        arredondado *= (Math.pow(10, 2));
        arredondado = Math.ceil(arredondado);
        arredondado /= (Math.pow(10, 2));
        return arredondado;
    }

    public Address buscarendereco(double latitude, double longitude) throws IOException{
        Geocoder geocoder;
        Address address = null;
        List<Address> addresses;

        geocoder = new Geocoder(getApplicationContext());
        addresses = geocoder.getFromLocation(latitude,longitude,1);
        if(addresses.size() > 0){
            address = addresses.get(0);
        }
        return address;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {
            dist = dist * 1.609344;
        } else if (unit.equals("N")) {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @SuppressLint("ObsoleteSdkInt")
    public void encerrarota() {
        Log.d("encerra**", "acabou");
        webView.loadUrl("https://vtaservice.com/mapas/conectcar3.php?versao=" + versao +"&s=sort&car=" + uisim);
        flagrota = "0";
        apagar task = new apagar();
        URL apaga = null;
        try {
            apaga = new URL("https://vtaservice.com/mapas/apaganotificacao.php?versao=" + versao + "&car=" + uisim +"&resposta=encerrada&os=" + os);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        task.execute(apaga);
        coordenadas = null;
        mTextView2.setText("");
        mMap.clear();
        novarota = "y";
        intervalo = 1000;
        os = "";
        stopService(new Intent(getBaseContext(), OsSave.class));
        Intent intent = new Intent(this, OsSave.class);
        stopService(intent);
        currentPolyline.remove();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("mapa**","456");
        mMap = googleMap;
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().isCompassEnabled();
        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(23.0f);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        LatLng loc = new LatLng(-22, -44);
        mMarker  = mMap.addMarker(new MarkerOptions().position(loc));

    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Iniciar Serviço")
                        .setMessage("Para iniciar a aplicação click em Allow na solicitação de permissão a seguir")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                recreate();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[]
            grantResults) {

//If the permission has been granted...//

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//...then start the GPS tracking service//

            onResume();
        } else {

//If the user denies the permission request, then display a toast with some more information//

            //  Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return String.format("https://maps.googleapis.com/maps/api/directions/%s?%s&key=%s", output, parameters, getString(R.string.google_maps_key));
    }


    @SuppressLint("StaticFieldLeak")
    protected static class apagar extends AsyncTask<URL, Void, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(URL... urls) {
            // implement API in background and store the response in current variable

            String current = "";
            try {
                URL url = urls[0];
                HttpURLConnection urlConnection = null;
                try {

                    urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    if(HttpURLConnection.HTTP_OK != urlConnection.getResponseCode()){
                        return null;
                    }

                    // return the data to onPostExecute method
                    return null;

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
            return current;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class viagem extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            /**
             * Pegamos o primeiro item da lista....
             */
            final String txtUrl = strings[0];

            try{
                /**
                 * Criamos a URL
                 */
                final URL url = new URL(txtUrl);
                /**
                 * Criamos a conexão com a URL
                 */
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                /**
                 * Infromamos o método de requisição
                 */
                assert con != null;
                con.setRequestMethod("GET");
                /**
                 * Se o código de resposta for diferente de OK (200)
                 * Então retornamos null;
                 */
                if(HttpURLConnection.HTTP_OK != con.getResponseCode()){
                    return null;
                }

                /**
                 * Vamos copiar o conteúdo do response....
                 */
                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                final StringBuilder buffer = new StringBuilder();
                while( (line = reader.readLine()) != null ){
                    buffer.append(line);
                }

                // Vamos transformar a String contendo o html em um objeto Document do Jsoup

                final Document document = Jsoup.parse(buffer.toString());

                // Vamos procurar as DIV's que tenham id (#) igual a coords

                final Elements divs = document.select("div#dadosviagem");


                //Se nulo, ou vazio, não encontrou, então retorna nulo!

                if(null == divs || divs.size() == 0 ){
                    return null;
                }


                //pegamos o primeiro elemento

                final Element div = divs.first();

                //Pegamos o texto deste elemento

                return div.text();

            }catch (final Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {

        }
    }

    @SuppressLint("StaticFieldLeak")
    class ordemservico extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            /**
             * Pegamos o primeiro item da lista....
             */
            final String txtUrl = strings[0];

            try{
                /**
                 * Criamos a URL
                 */
                final URL url = new URL(txtUrl);
                /**
                 * Criamos a conexão com a URL
                 */
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                /**
                 * Infromamos o método de requisição
                 */
                assert con != null;
                con.setRequestMethod("GET");
                /**
                 * Se o código de resposta for diferente de OK (200)
                 * Então retornamos null;
                 */
                if(HttpURLConnection.HTTP_OK != con.getResponseCode()){
                    return null;
                }

                /**
                 * Vamos copiar o conteúdo do response....
                 */
                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                final StringBuilder buffer = new StringBuilder();
                while( (line = reader.readLine()) != null ){
                    buffer.append(line);
                }

                // Vamos transformar a String contendo o html em um objeto Document do Jsoup

                final Document document = Jsoup.parse(buffer.toString());

                // Vamos procurar as DIV's que tenham id (#) igual a coords

                final Elements divs = document.select("div#os");


                //Se nulo, ou vazio, não encontrou, então retorna nulo!

                if(null == divs || divs.size() == 0 ){
                    return null;
                }


                //pegamos o primeiro elemento

                final Element div = divs.first();

                //Pegamos o texto deste elemento

                return div.text();

            }catch (final Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            Log.d("oS*****",  String.valueOf(s));
            if(s != null && !s.trim().equals("")) {
                os = s.trim();
                ligamonitoring = "ligado";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(getBaseContext(), OsSave.class));
                } else {
                    startService(new Intent(getBaseContext(), OsSave.class));
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class getcoordenadas extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {

            /**
             * Pegamos o primeiro item da lista....
             */
            final String txtUrl = strings[0];

            try {
                /**
                 * Criamos a URL
                 */
                final URL url = new URL(txtUrl);
                /**
                 * Criamos a conexão com a URL
                 */
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                /**
                 * Infromamos o método de requisição
                 */
                assert con != null;
                con.setRequestMethod("GET");
                /**
                 * Se o código de resposta for diferente de OK (200)
                 * Então retornamos null;
                 */
                if (HttpURLConnection.HTTP_OK != con.getResponseCode()) {
                    return null;
                }

                /**
                 * Vamos copiar o conteúdo do response....
                 */
                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                final StringBuilder buffer = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                // Vamos transformar a String contendo o html em um objeto Document do Jsoup

                final Document document = Jsoup.parse(buffer.toString());

                // Vamos procurar as DIV's que tenham id (#) igual a coords

                final Elements divs = document.select("div#coords");


                //Se nulo, ou vazio, não encontrou, então retorna nulo!

                if (null == divs || divs.size() == 0) {
                    return null;
                }


                //pegamos o primeiro elemento

                final Element div = divs.first();

                //Pegamos o texto deste elemento

                return div.text();

            } catch (final Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            Log.d("S*****", String.valueOf(s));
            if (s != null) {
                coordenadas = s;
                flagrota = "1";
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class motoristas extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            /**
             * Pegamos o primeiro item da lista....
             */
            final String txtUrl = strings[0];

            try{
                /**
                 * Criamos a URL
                 */
                final URL url = new URL(txtUrl);
                /**
                 * Criamos a conexão com a URL
                 */
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                /**
                 * Infromamos o método de requisição
                 */
                assert con != null;
                con.setRequestMethod("GET");
                /**
                 * Se o código de resposta for diferente de OK (200)
                 * Então retornamos null;
                 */
                if(HttpURLConnection.HTTP_OK != con.getResponseCode()){
                    return null;
                }

                /**
                 * Vamos copiar o conteúdo do response....
                 */
                final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                final StringBuilder buffer = new StringBuilder();
                while( (line = reader.readLine()) != null ){
                    buffer.append(line);
                }

                // Vamos transformar a String contendo o html em um objeto Document do Jsoup

                final Document document = Jsoup.parse(buffer.toString());

                // Vamos procurar as DIV's que tenham id (#) igual a coords

                final Elements divs = document.select("div#motoristas");


                //Se nulo, ou vazio, não encontrou, então retorna nulo!

                if(null == divs || divs.size() == 0 ){
                    return null;
                }


                //pegamos o primeiro elemento

                final Element div = divs.first();

                //Pegamos o texto deste elemento

                return div.text();

            }catch (final Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String s) {
            Log.d("motS*****", String.valueOf(s));
            mTextView.setText(s);

        }
    }


}