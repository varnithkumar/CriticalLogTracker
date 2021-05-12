package app.criticalog.tracker.reciever;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

import app.criticalog.tracker.interfaces.AlarmTriggered;

public class MyAlarmReciever extends BroadcastReceiver {

    Context temp;
    private OnItemClickListener mListener;
    AlarmTriggered alarmTriggered;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public OnItemClickListener getOnItemClickListener() {
        return mListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentBroadCast = new Intent("time_update");
        intentBroadCast.putExtra("isUpdated", true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentBroadCast);

    }


}