package com.ecttion.pilotoboradevan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.squareup.okhttp.OkHttpClient;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.ecttion.pilotoboradevan.MapsActivity.os;
import static com.ecttion.pilotoboradevan.MapsActivity.servidor;
import static com.ecttion.pilotoboradevan.MapsActivity.uisim;

@SuppressLint("Registered")
public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 5000;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public  String[] semconexao;
    public static int i;
    Intent intent;


    @Override
    public void onCreate() {


        super.onCreate();
        Log.e(TAG, "On Location***");
        semconexao =  new String[getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0).size()];
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


    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int startId, int flags) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(!os.equals("")) {
                return START_STICKY;
            }else {
                return START_NOT_STICKY;
            }

        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 50, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 50, listener);
        Log.e(TAG, "On Start***");
        if(!os.equals("")) {
            return START_STICKY;
        }else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 1;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.e("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);

        if(!os.equals("")){
            sendBroadcast(new Intent("YouWillNeverKillMe"));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        t.start();
        return t;
    }

    public class MyLocationListener implements LocationListener {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onLocationChanged(final Location loc) {
            Log.e("*****", "Location changed");

            if (isBetterLocation(loc, previousBestLocation)) {
                Log.e("***zdf**", String.valueOf(previousBestLocation));
                if(!os.equals("")) {

                    try {
                        Date data = new Date();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formataData = new SimpleDateFormat("yyyy/MM/dd%20HH:mm:ss");
                        String dataFormatada = formataData.format(data);
                        URL localizacaourl;

                        try {
                            localizacaourl = new URL(servidor + "&carro=" + uisim + "&latitude=" + loc.getLatitude() + "&longitude=" + loc.getLongitude() + "&velocidademts=" + loc.getSpeed() + "&momento=" + dataFormatada + "&os=" + os);
                            informar task = new informar();
                            Log.e("*****", "informando");

                            if (isConected(getApplicationContext())) {
                                Log.e("*****", "tem conexao");
                                Log.e("**i**", String.valueOf(i));
                                Log.e("**i**", String.valueOf(localizacaourl));
                                task.execute(localizacaourl);

                                while (i >= 0) {
                                    informar taskp = new informar();
                                    if (semconexao[i] != null) {
                                        Log.e("**u**", String.valueOf(semconexao[i]));
                                        URL myURL = new URL(semconexao[i]);
                                        taskp.execute(myURL);
                                        semconexao[i] = null;
                                    }
                                    i--;
                                }

                            } else {
                                Log.e("*****", "nao tem conexao");
                                semconexao[i] = servidor + "&carro=" + uisim + "&latitude=" + loc.getLatitude() + "&longitude=" + loc.getLongitude() + "&velocidademts=" + loc.getSpeed() + "&momento=" + dataFormatada + "&off=1" + "&os=" + os;
                                i++;
                                Log.e("**u**", String.valueOf(semconexao[i]));
                                Log.e("**i**", String.valueOf(i));
                            }


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
            sendBroadcast(intent);


        }

        boolean isConected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if ( cm != null ) {
                NetworkInfo ni = cm.getActiveNetworkInfo();

                return ni != null && ni.isConnected();
            }

            return false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
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
