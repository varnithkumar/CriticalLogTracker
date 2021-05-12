package app.criticalog.tracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.Nullable;

import java.util.Date;

import app.criticalog.tracker.R;
import app.criticalog.tracker.Utils.Config;
import app.criticalog.tracker.impl.ActivityLocationDaoImpl;
import app.criticalog.tracker.interfaces.ActivityLocationDao;
import app.criticalog.tracker.interfaces.LocationUpdateCallback;
import app.criticalog.tracker.model.ActivityLocation;
import app.criticalog.tracker.model.Dao.SqliteConnection;


public class TrackerService extends Service {
    public static String PACKAGE_NAME;
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = Config.CLNotificationId;
    private Notification.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private Location prevLocation = null;

    public static final String NOTIFICATION_CHANNEL_ID_SERVICE = PACKAGE_NAME + ".service";
    public static final String NOTIFICATION_CHANNEL_ID_INFO = PACKAGE_NAME + ".download_info";
    public static LocationUpdateCallback locationUpdateCallback;
    private static ActivityLocationDao activityLocationDao;
    private static app.criticalog.tracker.entity.Location currentLocation;

    private static final String NOTIFICATION_CHANNEL = "" + Config.CLNotificationId;
    PendingIntent broadcastIntent;

    @Override

    public void onCreate() {
        super.onCreate();
        PACKAGE_NAME = getApplicationContext().getPackageName();
        String stop = "stop";
        activityLocationDao = new ActivityLocationDaoImpl(new SqliteConnection(this).getWritableDatabase());

        registerReceiver(stopReceiver, new IntentFilter(stop));
        broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        activityLocationDao = new ActivityLocationDaoImpl(new SqliteConnection(this).getWritableDatabase());

        try {

            buildNotification();
            requestLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    private void requestLocationUpdates() {
        initLocationCallback(new LocationUpdateCallback() {
            @Override
            public void onLocation(Location location) {
                try {
                    currentLocation = new app.criticalog.tracker.entity.Location(location);
                    saveActivityLocation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAddress(String address) {
            }

            @Override
            public void onError(String error) {

            }


        });

        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

        request.setInterval(1000 * 60 * 10);
        request.setFastestInterval(1000 * 60 * 10);

//Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission == PackageManager.PERMISSION_GRANTED) {


            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {


                    Location location = locationResult.getLastLocation();

                    if (locationUpdateCallback != null) {
                        locationUpdateCallback.onLocation(location);
            /*locationUpdateCallback.onAddress(Utils.getLocalAddress(getApplicationContext(),
                    activityLocation.getLocation().getLatitude(), activityLocation.getLocation().getLongitude()));*/
                    }

                }
            }, null);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void initLocationCallback(LocationUpdateCallback locationUpdateCallback1) {
        locationUpdateCallback = locationUpdateCallback1;
    }


    private void saveActivityLocation() {

        if (currentLocation != null) {

            ActivityLocation activityLocation = new ActivityLocation();
            activityLocation.setLocation(currentLocation);
            activityLocation.setDate(new Date());
            activityLocationDao.insert(activityLocation);
            //  updateLocationUI(activityLocation);

        }


    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String action = intent.getStringExtra("Action");
                if (action.equals("stop")) {

//Unregister the BroadcastReceiver when the notification is tapped//
                    unregisterReceiver(stopReceiver);
//Stop the Service//
                    stopLocationUpdates();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    public void stopLocationUpdates() {
        try {

            activityLocationDao.deleteAll();
            stopForeground(true);
            stopSelf();
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
            unregisterReceiver(stopReceiver);


        } catch (Exception e) {

        }

    }

    private void buildNotification() {
        initChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, getNotification());
        } else {
            getNotification();
        }

    }

    public void initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID_SERVICE, "App Service", NotificationManager.IMPORTANCE_LOW));
            nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID_INFO, "Download Info", NotificationManager.IMPORTANCE_LOW));
        }
    }

    @SuppressLint("NewApi")
    private Notification getNotification() {
        String PACKAGE_NAME = getApplicationContext().getPackageName();
        int launcher_icon = R.drawable.ic_launcher_background;
        int colorFilter = R.color.colorAccent;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            launcher_icon = R.drawable.icon;
            colorFilter = R.color.colorAccent;
        } else
            launcher_icon = R.drawable.ic_launcher_background;


        try {

            String appName = getApplicationContext().getResources().getString(R.string.app_name);

            this.mBuilder = new Notification.Builder(this);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.mBuilder.setSmallIcon(launcher_icon);
                this.mBuilder.setColor(getResources().getColor(colorFilter));
            } else {
                this.mBuilder.setSmallIcon(launcher_icon);
            }
            this.mBuilder.setOngoing(true);
            this.mBuilder.setContentIntent(broadcastIntent);

            this.mBuilder.setContentTitle(appName + " is online").setContentText(appName).setAutoCancel(false).setOngoing(true);
            this.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {

                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, appName, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(-65536);
                notificationChannel.setShowBadge(true);
                assert this.mNotificationManager != null;
                this.mBuilder.setChannelId(NOTIFICATION_CHANNEL);
                this.mNotificationManager.createNotificationChannel(notificationChannel);
            }

            assert this.mNotificationManager != null;
            NotificationManager notificationManager1 = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager1.notify(NOTIFICATION_ID, this.mBuilder.build());
            // startForeground(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception var6) {

            System.out.println("service exception " + var6.getMessage());
        }
        return mBuilder.build();
    }
}
