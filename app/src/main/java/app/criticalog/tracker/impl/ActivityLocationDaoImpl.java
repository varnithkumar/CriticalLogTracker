package app.criticalog.tracker.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.criticalog.tracker.entity.Location;
import app.criticalog.tracker.interfaces.ActivityLocationDao;
import app.criticalog.tracker.model.ActivityLocation;
import app.criticalog.tracker.model.Dao.Dao;

public class ActivityLocationDaoImpl extends Dao implements ActivityLocationDao {

    private static final String TABLE_NAME = "criticallog_location";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LATIDUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_DATE = "date";
    private static final String[] COLUMN_ARRAY = new String[]{COLUMN_ID, COLUMN_LATIDUDE, COLUMN_LONGITUDE,
             COLUMN_DATE};
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (\n" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            COLUMN_LATIDUDE + " INTEGER NOT NULL,\n" +
            COLUMN_LONGITUDE + " INTEGER NOT NULL,\n" +
            COLUMN_DATE + " INTEGER NOT NULL\n" +
            ")";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public ActivityLocationDaoImpl(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public boolean insert(ActivityLocation activityLocation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LATIDUDE, activityLocation.getLocation().getLatitude());
        contentValues.put(COLUMN_LONGITUDE, activityLocation.getLocation().getLongitude());
        contentValues.put(COLUMN_DATE, activityLocation.getDate().getTime());
        return getDatabase().insert(TABLE_NAME, null, contentValues) != -1;
    }

    @Override
    public void deleteAll() {

     //   getDatabase().delete(TABLE_NAME, null, null);
        getDatabase().execSQL("delete from "+ TABLE_NAME);
    }

    @Override
    public List<ActivityLocation> listAll(Date currentDay) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        currentDay = calendar.getTime();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        Date nextDay = calendar.getTime();

        Cursor cursor = getDatabase().query(TABLE_NAME, COLUMN_ARRAY, COLUMN_DATE + " > ? AND " + COLUMN_DATE + " < ?",
                new String[]{String.valueOf(currentDay.getTime()), String.valueOf(nextDay.getTime())},
                null, null, COLUMN_DATE + " ASC", null);

        List<ActivityLocation> activityLocationList = new ArrayList<>();
        while (cursor.moveToNext()) {

            activityLocationList.add(convertCursorToEntity(cursor));
        }
        return activityLocationList;
    }

    @Override
    public List<ActivityLocation> listAll(Date startDate, Date finalDate) {

        Cursor cursor = getDatabase().query(TABLE_NAME, COLUMN_ARRAY, COLUMN_DATE + " > ? AND " + COLUMN_DATE + " < ?",
                new String[]{String.valueOf(startDate.getTime()), String.valueOf(finalDate.getTime())},
                null, null, COLUMN_DATE + " ASC", null);

        List<ActivityLocation> activityLocationList = new ArrayList<>();
        while (cursor.moveToNext()) {
            activityLocationList.add(convertCursorToEntity(cursor));
        }
        return activityLocationList;
    }


    public ActivityLocation convertCursorToEntity(Cursor cursor) {
        ActivityLocation activityLocation = new ActivityLocation();
        Location location = new Location();

        activityLocation.setId(cursor.getInt(0));
        location.setLatitude(cursor.getDouble(1));
        location.setLongitude(cursor.getDouble(2));
        activityLocation.setLocation(location);
        activityLocation.setDate(new Date(cursor.getLong(3)));

        return activityLocation;
    }


}
