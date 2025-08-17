package lb.edu.ul.mobileproject.home;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;
public class HomeFragment extends Fragment {

    private TextView updatedDate, monDate, tueDate, wedDate, thuDate, friDate, satDate, sunDate;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        updatedDate = root.findViewById(R.id.updatedDate);
        monDate = root.findViewById(R.id.monDate);
        tueDate = root.findViewById(R.id.tueDate);
        wedDate = root.findViewById(R.id.wedDate);
        thuDate = root.findViewById(R.id.thuDate);
        friDate = root.findViewById(R.id.friDate);
        satDate = root.findViewById(R.id.satDate);
        sunDate = root.findViewById(R.id.sunDate);
        recyclerView = root.findViewById(R.id.rv);

        updateMonthYear();
        highlightCurrentDay();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Main.database.eventDao().getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                Log.d("EventData", "Number of events: " + events.size());
                HomeAdapter adapter = new HomeAdapter(events);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            } else {
                Log.d("EventData", "No events found");
                Toast.makeText(getContext(), "No events available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String currentMonthYear = monthYearFormat.format(calendar.getTime());
        updatedDate.setText(currentMonthYear);
    }

    private void highlightCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        resetDayBackgrounds();
        GradientDrawable highlightDrawable = new GradientDrawable();
        highlightDrawable.setShape(GradientDrawable.RECTANGLE);
        highlightDrawable.setCornerRadius(1000f);
        highlightDrawable.setColor(Color.parseColor("#2196F3"));

        switch (currentDayOfWeek) {
            case Calendar.MONDAY:
                setDayHighlighted(monDate, highlightDrawable);
                break;
            case Calendar.TUESDAY:
                setDayHighlighted(tueDate, highlightDrawable);
                break;
            case Calendar.WEDNESDAY:
                setDayHighlighted(wedDate, highlightDrawable);
                break;
            case Calendar.THURSDAY:
                setDayHighlighted(thuDate, highlightDrawable);
                break;
            case Calendar.FRIDAY:
                setDayHighlighted(friDate, highlightDrawable);
                break;
            case Calendar.SATURDAY:
                setDayHighlighted(satDate, highlightDrawable);
                break;
            case Calendar.SUNDAY:
                setDayHighlighted(sunDate, highlightDrawable);
                break;
        }

        // Update day text for the week
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            Calendar dayCalendar = (Calendar) calendar.clone();
            dayCalendar.set(Calendar.DAY_OF_WEEK, i);
            int dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH);
            String dayName = getDayName(i);
            updateDayText(i, dayName, dayOfMonth);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateDayText(int day, String dayName, int dayOfMonth) {
        switch (day) {
            case Calendar.MONDAY:
                monDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.TUESDAY:
                tueDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.WEDNESDAY:
                wedDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.THURSDAY:
                thuDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.FRIDAY:
                friDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.SATURDAY:
                satDate.setText(dayName + "\n" + dayOfMonth);
                break;
            case Calendar.SUNDAY:
                sunDate.setText(dayName + "\n" + dayOfMonth);
                break;
        }
    }

    private void resetDayBackgrounds() {
        GradientDrawable defaultDrawable = new GradientDrawable();
        defaultDrawable.setShape(GradientDrawable.RECTANGLE);
        defaultDrawable.setCornerRadius(1000f);
        defaultDrawable.setColor(Color.WHITE);
        setDayReset(monDate, defaultDrawable);
        setDayReset(tueDate, defaultDrawable);
        setDayReset(wedDate, defaultDrawable);
        setDayReset(thuDate, defaultDrawable);
        setDayReset(friDate, defaultDrawable);
        setDayReset(satDate, defaultDrawable);
        setDayReset(sunDate, defaultDrawable);
    }

    private void setDayReset(TextView dayView, GradientDrawable drawable) {
        dayView.setBackground(drawable);
        dayView.setTextColor(Color.BLACK);
    }

    private void setDayHighlighted(TextView dayView, GradientDrawable drawable) {
        dayView.setBackground(drawable);
        dayView.setTextColor(Color.WHITE);
    }

    private String getDayName(int day) {
        switch (day) {
            case Calendar.MONDAY:
                return "MON";
            case Calendar.TUESDAY:
                return "TUE";
            case Calendar.WEDNESDAY:
                return "WED";
            case Calendar.THURSDAY:
                return "THU";
            case Calendar.FRIDAY:
                return "FRI";
            case Calendar.SATURDAY:
                return "SAT";
            case Calendar.SUNDAY:
                return "SUN";
            default:
                return "";
        }
    }
}