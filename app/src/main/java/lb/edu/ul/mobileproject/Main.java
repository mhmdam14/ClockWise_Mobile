package lb.edu.ul.mobileproject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lb.edu.ul.mobileproject.calendar.CalendarFragment;
import lb.edu.ul.mobileproject.database.AppDatabase;
import lb.edu.ul.mobileproject.database.Event;
import lb.edu.ul.mobileproject.drawer.ContactFragment;
import lb.edu.ul.mobileproject.drawer.ProfileFragment;
import lb.edu.ul.mobileproject.home.HomeFragment;
import lb.edu.ul.mobileproject.recording.RecordingsFragment;
import lb.edu.ul.mobileproject.sync_data.StoreData;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static String ip= "192.168.43.163";
    int current_day,current_month,current_year;
    DrawerLayout drawerlayout;
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    SharedPreferences sp;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    FloatingActionButton fab;
    public static AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab=findViewById(R.id.fab);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sp=getSharedPreferences("credentials",MODE_PRIVATE);


        drawerlayout=findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.navigation_drawer_open
                ,R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView=findViewById(R.id.navigation_drawer);

        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView=findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.bottom_home) {
                    openFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.bottom_Calender) {
                    openFragment(new CalendarFragment());
                    return true;
                } else if (itemId == R.id.bottom_Recordings) {
                    openFragment(new RecordingsFragment());
                    return true;
                } else if (itemId == R.id.bottom_study) {
                    openFragment(new StudyFragment());
                    return true;
                }
                return false;
            }
        });
        fragmentManager =getSupportFragmentManager();
        openFragment(new HomeFragment());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, AddEvent.class);
                startActivity(i);
            }
        });
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "Events").build();
        current_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        current_month=Calendar.getInstance().get(Calendar.MONTH);
        current_year=Calendar.getInstance().get(Calendar.YEAR);
        executorService.execute(() -> {
            List<Event> events = database.eventDao().getListOfEvents();
            checkEventDate(current_day,current_month,current_year, events);
        });
        if(!CheckConnection.isInternetConnection(this))
        {
            StoreData storeData = new StoreData(this);
            storeData.storeInDb();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items, menu);

        // Update toolbar item visibility based on current connection status
        boolean isConnected = CheckConnection.isInternetConnection(this);
        menu.findItem(R.id.no_internet).setVisible(isConnected);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();
        if(itemId==R.id.nav_profile)
            openFragment(new ProfileFragment());
        else if (itemId==R.id.nav_logout) {
            SharedPreferences.Editor ed=sp.edit();
            ed.putString("email","");
            ed.putString("password","");
            ed.apply();
            finish();
        } else if (itemId==R.id.nav_contact) {
            openFragment(new ContactFragment());
        }
        drawerlayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerlayout.isDrawerOpen(GravityCompat.START)){
            drawerlayout.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }

    }
    private void openFragment(Fragment fragment)
    {
        FragmentTransaction transaction;
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container,fragment);
        transaction.commit();
    }
    public void checkEventDate(int current_day,int current_month,int current_year, List<Event> events) {
        for (Event event : events) {
            String date = event.getEventDate();
            String [] format=date.split("-");
            int day = Integer.parseInt(format[2]);
            int month=Integer.parseInt(format[1]);
            int year=Integer.parseInt(format[0]);
            if (current_day > day&&current_month+1>=month&&current_year>=year) {
                executorService.execute(() -> database.eventDao().delete(event));
            }
        }
    }


}