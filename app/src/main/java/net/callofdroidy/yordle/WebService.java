package net.callofdroidy.yordle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import fi.iki.elonen.SimpleWebServer;

/**
 * Created by yli on 12/04/17.
 */

public class WebService extends Service {
    private static final String TAG = "ServiceWeb";

    private NotificationManager mNM;

    int NOTIFICATION_ID = 101;

    private SimpleWebServer simpleServer;

    public class LocalBinder extends Binder{
        WebService getServce(){
            return WebService.this;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        int ipAddr = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress();
        String formattedIpAddr = String.format(Locale.CANADA, "%d.%d.%d.%d", (ipAddr & 0xff), (ipAddr >> 8 & 0xff),
                (ipAddr >> 16 & 0xff), (ipAddr >> 24 & 0xff));
        Log.e(TAG, "onCreate: ip addr: " + formattedIpAddr);

        String rootDirPath = getSharedPreferences("serverConfig", 0).getString("rootDir", Environment.getExternalStorageDirectory().toString());
        File rootDir =  new File(rootDirPath);
        try{
            simpleServer = new SimpleWebServer(formattedIpAddr, 8088, rootDir, false);
            simpleServer.start();

            Log.e(TAG, "onCreate: is server alive: " + simpleServer.isAlive());
        }catch (IOException e){
            e.printStackTrace();
        }
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "onStartCommand: ");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        simpleServer.stop();
        mNM.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    private void showNotification() {

        CharSequence text = "Server started";

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivityMain.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_radio_button_checked_black_24dp)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Server is running")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION_ID, notification);
    }
}
