package app.criticalog.tracker.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class Config {
    public static int CLNotificationId = 10001;
    public static int LOCATION_PERMISSION=3;

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + " : " + serviceClass.getSimpleName());
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + " : " + serviceClass.getName());
        return false;
    }

}
