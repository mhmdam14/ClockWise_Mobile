package lb.edu.ul.mobileproject.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM event WHERE SyncStatus!='PENDING_DELETE' ORDER BY  DateTime")
    LiveData<List<Event>> getAllEvents();

    @Insert
    void insertAll(Event... events);

    @Delete
    void delete(Event event);

    @Update
    void update(Event event);

    @Query("SELECT * FROM event WHERE event.id=:id")
    Event getEventById(int id);

    @Query("SELECT * FROM event")
    List<Event> getListOfEvents();

    @Query("SELECT * FROM event WHERE  eventName=:eventName AND eventDescription=:eventDescription AND eventDate=:eventDate AND eventStartTime=:eventStartTime AND eventFinishTime=:eventFinishTime")
    Event getEventByALL(String eventName,String eventDescription,String eventDate,String eventStartTime,String eventFinishTime);
}
