package app.criticalog.tracker.interfaces;

import android.location.Location;

public interface LocationUpdateCallback {

    void onLocation(Location location);

    void onAddress(String address);

    void onError(String error);
}