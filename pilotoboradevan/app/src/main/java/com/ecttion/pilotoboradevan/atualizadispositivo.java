package com.ecttion.pilotoboradevan;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint("Registered")
public class atualizadispositivo extends Service {
    private HandlerThread handlerThread;
    private Handler handler;
    private final int TempoAtualizaDisp = 1;
    public static String visitors = "";

    @Override
    public void onCreate() {
        handlerThread = new HandlerThread("HandlerThread");

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Previne que seja executado em subsequentes chamadas a onStartCommand
        if (!handlerThread.isAlive()) {

            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            Runnable runnable = new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    Log.d("NotifyService", "Notificando");
                    sendNotification();
                    handler.postDelayed(this, 30000 * TempoAtualizaDisp);
                }
            };
            handler.post(runnable);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NotifyService", "Notificações terminadas");
        handlerThread.quit();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sendNotification() {
        Log.d("userons", "acessando");
        monitora task = new monitora();
        URL online = null;
        try {
            online = new URL("https://boradevan.com/interface2/useronline.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        task.execute(String.valueOf(online));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @SuppressLint("StaticFieldLeak")
    class monitora extends AsyncTask<String, Void, String> {

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

                final Elements divs = document.select("div#on");


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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String s) {
            Log.d("posexecute", String.valueOf(s));
            visitors = String.valueOf(s);
            if(!visitors.equals("0")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(getBaseContext(), TrackingService.class));
                } else {
                    startService(new Intent(getBaseContext(), TrackingService.class));

                }
            }
            if(visitors.equals("0")){
                Log.d("parar", String.valueOf(s));
                stopService(new Intent(getBaseContext(), TrackingService.class));
                Log.d("parar", String.valueOf(getBaseContext()));
            }
        }
    }

}