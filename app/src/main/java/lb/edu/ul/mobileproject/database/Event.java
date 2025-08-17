package lb.edu.ul.mobileproject.database;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class Event {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name="EventName")
    private String eventName;

    @ColumnInfo(name="EventDescription")
    private String eventDescription;

    @ColumnInfo(name="EventDate")
    private String  eventDate;

    @ColumnInfo(name="EventStartTime")
    private String eventStartTime;

    @ColumnInfo(name="EventFinishTime")
    private String eventFinishTime;

    @ColumnInfo(name="DateTime")
    private String eventDateTime;

    @ColumnInfo(name="SyncStatus")
    private String syncStatus;



    public int getId() {
        return id;
    }

    public String getEventFinishTime() {
        return eventFinishTime;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventName() {
        return eventName;
    }

    public String getSyncStatus(){ return syncStatus;}

    public void setEventFinishTime(String eventFinishTime) {
        this.eventFinishTime = eventFinishTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSyncStatus(String syncStatus){this.syncStatus = syncStatus;}
}
