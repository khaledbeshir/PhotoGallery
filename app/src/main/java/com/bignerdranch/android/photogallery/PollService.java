
package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Mohamed Amr on 5/23/2019.
 */

public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000*30; //60 second
    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context , PollService.class);
        return intent;
    }

    public PollService(){
         super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(!isNetworkAvailableandConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getlastResultId(this);
        List<GalleryItem> items;

        if (query == null){
            items = new FlickerFitcher().fetchRecentPhotos();
        }else{
            items = new FlickerFitcher().searchPhotos(query);
        }
        if (items.size() == 0){
            return;
        }
        String resultid = items.get(0).getId();
        if (resultid.equals(lastResultId)){
            Log.i(TAG , "GOT an old result" + resultid);
        }
        else{
            Log.i(TAG , "Got a new result "+ resultid);
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.NewIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this , 0 ,i , 0 );
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManagerCompat=
                    NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(0,notification);
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));
        }
        QueryPreferences.setLastResultId(this , lastResultId);
    }

    private boolean isNetworkAvailableandConnected (){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null ;
        boolean isNetworkConnected = cm.getActiveNetworkInfo().isConnected() &&
                isNetworkAvailable ;
        return isNetworkConnected;
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i , PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }

    public static void setServiceAlarm (Context context , boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context ,0 ,i , 0 );

        AlarmManager alarmManager =(AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        if(isOn){
            Log.i(TAG , "before Alarm");
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME , SystemClock.elapsedRealtime()
            ,POLL_INTERVAL , pi);
            Log.i(TAG , "after Alarm");
        }else {
            Log.i(TAG , "cancel Alarm");
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreferences.setAlarmOn(context , isOn);
    }

}
