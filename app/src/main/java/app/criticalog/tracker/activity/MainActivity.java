package app.criticalog.tracker.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.criticalog.tracker.R;
import app.criticalog.tracker.Utils.Config;
import app.criticalog.tracker.impl.ActivityLocationDaoImpl;
import app.criticalog.tracker.interfaces.ActivityLocationDao;
import app.criticalog.tracker.interfaces.LocationUpdateCallback;
import app.criticalog.tracker.model.ActivityLocation;
import app.criticalog.tracker.model.Dao.SqliteConnection;
import app.criticalog.tracker.reciever.MyAlarmReciever;
import app.criticalog.tracker.services.TrackerService;

import static app.criticalog.tracker.Utils.Config.LOCATION_PERMISSION;

public class MainActivity extends AppCompatActivity {

    TextView textView, btnLoad;
    private static ActivityLocationDao activityLocationDao;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private boolean isRunningService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.txt_location);
        btnLoad = findViewById(R.id.btn_load);
        activityLocationDao = new ActivityLocationDaoImpl(new SqliteConnection(this).getWritableDatabase());
        checkServiceRunning();
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRunningService) {
                    stopLocationUpdate();
                } else {
                    checkPermission();
                }
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("time_update"));

    }

    private void stopLocationUpdate() {
        try {

            if (Config.isMyServiceRunning(MainActivity.this, TrackerService.class)) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("stop");
                broadcastIntent.putExtra("Action", "stop");
                this.sendBroadcast(broadcastIntent);
            }
            if (alarmMgr != null && alarmIntent != null) {
                alarmMgr.cancel(alarmIntent);
            }
            activityLocationDao.deleteAll();
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                public void run() {
                    setData();
                    checkServiceRunning();
                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkServiceRunning() {
        if (Config.isMyServiceRunning(getApplicationContext(), TrackerService.class)) {
            btnLoad.setText(getResources().getString(R.string.stop_tracking));
            isRunningService = true;
        } else {
            btnLoad.setText(getResources().getString(R.string.start_tracker));
            isRunningService = false;
            if (activityLocationDao!=null)
            activityLocationDao.deleteAll();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isUpdated = intent.getBooleanExtra("isUpdated", false);
            if (isUpdated) {
                setData();

            }
        }
    };

    private void setData() {
        Gson gson = new Gson();
        List<ActivityLocation> activityLocationListOfToday = activityLocationDao.listAll(new Date());
        String loginGson = gson.toJson(activityLocationListOfToday);
        textView.setText(loginGson);
    }

    private void startLocationService() {
        if (!Config.isMyServiceRunning(MainActivity.this, TrackerService.class)) {
            startService(new Intent(this, TrackerService.class));
            setAlarm();

        }
        checkServiceRunning();
    }

    private void setAlarm() {

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyAlarmReciever.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        //   calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
       /* alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                , alarmIntent);*/
    }

    private void checkPermission() {
        int permissionFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,}, LOCATION_PERMISSION);
            }

        } else {
            startLocationService();

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            try {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                } else {
                    checkPermission();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
