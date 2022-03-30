package com.ecttion.pilotoboradevan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.OkHttpClient;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.ecttion.pilotoboradevan.LocationService.BROADCAST_ACTION;
import static com.ecttion.pilotoboradevan.MapsActivity.os;
import static com.ecttion.pilotoboradevan.MapsActivity.servidor;
import static com.ecttion.pilotoboradevan.MapsActivity.uisim;
import static com.ecttion.pilotoboradevan.atualizadispositivo.visitors;


public class OsSave extends Service {

    private static final String TAG = OsSave.class.getSimpleName();
    public  String[] semconexao;
    public static int i = 0;
    Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!os.equals("")){
            return START_STICKY;
        }else{
            stopSelf();
            return START_NOT_STICKY;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("acabaos", String.valueOf(os));
        stopSelf();
        if(!os.equals("")){
            sendBroadcast(new Intent("YouWillNeverKillMe"));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("parandod", String.valueOf(visitors));
        if(!os.equals("")){
            stopSelf();
            unregisterReceiver(stopReceiver);
            sendBroadcast(new Intent("YouWillNeverKillMe"));
        }
    }
    public void onPause(){


    }
    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        Log.e("**ossave***", "start");
        super.onCreate();
        semconexao =  new String[100000];
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        assert tm != null;

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        tmDevice = "" + tm.getDeviceId();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uisim = deviceUuid.toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "channel_01";
            String CHANNEL_NAME = "Channel Name";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID).setSound(null).setVibrate(new long[]{0});
            notification.setChannelId(CHANNEL_ID);

            startForeground(1, notification.build());
        }
        intent = new Intent(BROADCAST_ACTION);
        requestLocationUpdates();

    }

//Create the persistent notification//

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };


//Initiate the request to track the device's location//

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void requestLocationUpdates() {
        if (!uisim.equals("")){
            LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

            request.setInterval(MapsActivity.intervalo);
            Log.e("**inter**", String.valueOf(MapsActivity.intervalo));
//Get the most accurate location data available//

            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            final String path = "/databases/" + uisim;
            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//

            if (permission == PackageManager.PERMISSION_GRANTED) {

//...then request location updates//

                client.requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                        Location location = locationResult.getLastLocation();
                        if (location != null) {

//Save the location data to the database//
                            if(!os.equals("")) {
                                try {
                                    Date data = new Date();
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formataData = new SimpleDateFormat("yyyy/MM/dd%20HH:mm:ss");
                                    String dataFormatada = formataData.format(data);
                                    URL localizacaourl;

                                    try {
                                        localizacaourl = new URL(servidor + "&carro=" + uisim + "&latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude() + "&velocidademts=" + location.getSpeed() + "&momento=" + dataFormatada + "&os=" + os);
                                        informar task = new informar();
                                        Log.e("*****", "informando");

                                        if (isConected(getApplicationContext())) {
                                            Log.e("*****", "tem conexao");
                                            Log.e("**i**", String.valueOf(i));
                                            Log.e("**i**", String.valueOf(localizacaourl));
                                            while (i > 0) {
                                                informar taskp = new informar();
                                                if(semconexao[i] != null) {
                                                    URL myURL = new URL(semconexao[i]);
                                                    taskp.execute(myURL);
                                                    semconexao[i] = null;
                                                }
                                                i--;
                                            }
                                            task.execute(localizacaourl);
                                        } else {
                                            Log.e("*****", "nao tem conexao");
                                            Log.e("**i***", String.valueOf(i));
                                            semconexao[i] = servidor + "&carro=" + uisim + "&latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude() + "&velocidademts=" + location.getSpeed() + "&momento=" + dataFormatada + "&off=off" + "&os=" + os;
                                            Log.e("urlsem", String.valueOf(semconexao[i]));
                                            i++;
                                        }
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }, null);
            }
        }
    }

    boolean isConected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( cm != null ) {
            NetworkInfo ni = cm.getActiveNetworkInfo();

            return ni != null && ni.isConnected();
        }

        return false;
    }

    protected static class informar extends AsyncTask<URL, Void, String> {
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
}