package com.recoveryrecord.surveyandroid.example;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import static com.recoveryrecord.surveyandroid.example.Constants.DEFAULT_ESM_CHANNEL_ID;
import static com.recoveryrecord.surveyandroid.example.Constants.ESM_CHANNEL_ID;
import static com.recoveryrecord.surveyandroid.example.Constants.ESM_INTERVAL;
import static com.recoveryrecord.surveyandroid.example.Constants.ESM_TIME_OUT;
import static com.recoveryrecord.surveyandroid.example.Constants.VIBRATE_EFFECT;

public class AlarmReceiver extends BroadcastReceiver {
    //temp for notification
//    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
//    private final static String default_notification_channel_id = "default" ;
    String device_id = "";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        //send esm
        if(check_esm_time_range(context)){
//            select_news();
            Log.d("lognewsselect", "check_daily_time_range success");
            try {
                scheduleNotification_esm(context, getNotification_esm(context, "Please fill out the questionnaire" ), 1000);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
//                    Log.d("lognewsselect", "check_daily_time_range success");
//                    // Actions to do after 10 seconds
//                    try {
//                        Log.d("lognewsselect", "check_daily_time_range success");
////                        scheduleNotification_esm(getNotification_esm("Please fill out the questionnaire" ), 1000);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 3*1000);

        } else {
            Log.d("lognewsselect", "check_daily_time_range failed");

        }
    }

    private boolean check_esm_time_range(Context context){
        Long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        int MinHour = 9;//09:00:00
        int MaxHour = 22;//23:00:00
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long LastEsmTime = sharedPrefs.getLong("LastEsmTime", 0L);
        if(c.get(Calendar.HOUR_OF_DAY) >= MinHour && c.get(Calendar.HOUR_OF_DAY) < MaxHour) {
            Log.d("lognewsselect", "in daily interval");
            if(now - LastEsmTime > ESM_INTERVAL){
                Log.d("lognewsselect", "in hour interval");
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putLong("LastEsmTime", now);
                editor.apply();
                return true;
            } else {
                Log.d("lognewsselect", "not in hour interval");
                return false;
            }
        } else {
            Log.d("lognewsselect", "not in interval");
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("HardwareIds")
    private Notification getNotification_esm(Context context, String content) throws JSONException {

        Date date = new Date(System.currentTimeMillis());
        String esm_id = "";
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String time_now = formatter.format(date);
        esm_id = time_now;

        Intent intent_esm = new Intent();
//        intent_esm.setClass(NewsHybridActivity.this, ESMActivity.class);
        intent_esm.setClass(context, LoadingPageActivity.class);
        intent_esm.putExtra("esm_id", esm_id);
        int nid = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, nid, intent_esm, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_ESM_CHANNEL_ID);
        builder.setContentTitle("ESM");
        builder.setContentText("是時候填寫問卷咯~");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setChannelId(ESM_CHANNEL_ID);
        builder.setVibrate(VIBRATE_EFFECT);              //震動模式
        builder.setTimeoutAfter(ESM_TIME_OUT);           //自動消失 15*60*1000
        builder.setPriority(NotificationManager.IMPORTANCE_MAX);
        builder.setCategory(Notification.CATEGORY_REMINDER);
        Bundle extras = new Bundle();
        extras.putString("id", esm_id);
        extras.putString("type", "esm");
        builder.setExtras(extras);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String device_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Map<String, Object> esm = new HashMap<>();
        esm.put("noti_timestamp", Timestamp.now());
        esm.put("sample", 0);
        db.collection("test_users")
                .document(device_id)
                .collection("push_esm")
                .document(esm_id)
                .set(esm);
        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleNotification_esm (Context context, Notification notification, int delay) {
        Intent notificationIntent = new Intent(context, NotificationListenerNews.class);
        notificationIntent.putExtra(NotificationListenerESM.NOTIFICATION_ID, 1 ) ;
        notificationIntent.putExtra(NotificationListenerESM.NOTIFICATION, notification) ;
        int randomNum = ThreadLocalRandom.current().nextInt(0, 1000000 + 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, randomNum, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }
}
