package lb.edu.ul.mobileproject.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Event.class, Recording.class}, version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
    public abstract RecordingDao recordingDao();
}