package lb.edu.ul.mobileproject.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecordingDao {
    @Query("SELECT * FROM recording")
    LiveData<List<Recording>> getAllRecordings();

    @Insert
    void insert(Recording recording);

    @Delete
    void delete(Recording recording);
}