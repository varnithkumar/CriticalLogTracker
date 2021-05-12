package app.criticalog.tracker.interfaces;

import java.util.Date;
import java.util.List;

import app.criticalog.tracker.model.ActivityLocation;

public interface ActivityLocationDao {
    boolean insert(ActivityLocation activityLocation);

    void deleteAll();

    List<ActivityLocation> listAll(Date currentDay);

    List<ActivityLocation> listAll(Date startDate, Date finalDate);

}
