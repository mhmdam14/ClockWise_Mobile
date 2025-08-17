package lb.edu.ul.mobileproject.database;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recording")
public class Recording {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "file_name")
    private String fileName;

    @ColumnInfo(name = "file_path")
    private String filePath;

    public Recording(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}