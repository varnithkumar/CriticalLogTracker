package app.criticalog.tracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import app.criticalog.tracker.entity.Location;

public class ActivityLocation  implements Parcelable {

    private Integer id;
    private Date date;
    private Location location;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeParcelable(this.location, 0);
    }

    public ActivityLocation() {
    }

    protected ActivityLocation(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        int tmpActivityType = in.readInt();
        this.location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Parcelable.Creator<ActivityLocation> CREATOR = new Parcelable.Creator<ActivityLocation>() {
        public ActivityLocation createFromParcel(Parcel source) {
            return new ActivityLocation(source);
        }

        public ActivityLocation[] newArray(int size) {
            return new ActivityLocation[size];
        }
    };
}
