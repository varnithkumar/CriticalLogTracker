package app.criticalog.tracker.interfaces;

import android.location.Location;

import java.util.Date;
import java.util.List;

import app.criticalog.tracker.model.ActivityLocation;

public interface AlarmTriggered {

    List<ActivityLocation> listAll();
}
