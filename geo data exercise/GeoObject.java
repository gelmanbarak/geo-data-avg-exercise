import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeoObject {
    final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    String place;
    long timestamp;
    String magnitude;
    Double mag;
    String title;
    Date date;


    public Double getMag() {
        return mag;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public Date getDate() {
        try {
            return formatter.parse(formatter.format(date));
        }catch (ParseException e) {
            return null;
        }

    }

    public void setDate(Date date) {
       this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(String magnitude) {
        this.magnitude = magnitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
