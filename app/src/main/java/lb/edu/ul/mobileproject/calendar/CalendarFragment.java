package lb.edu.ul.mobileproject.calendar;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;
import lb.edu.ul.mobileproject.database.Event;
public class CalendarFragment extends Fragment {
    RecyclerView calendar_rv;
    NumberPicker monthPicker, yearPicker;
    List<Event> events = new ArrayList<>();
    int currentYear, currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendar_rv = root.findViewById(R.id.calendar_rv);
        monthPicker = root.findViewById(R.id.month_picker);
        yearPicker = root.findViewById(R.id.year_picker);

        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear);
        yearPicker.setMaxValue(currentYear + 50);
        yearPicker.setValue(currentYear);

        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        monthPicker.setValue(currentMonth);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CalendarAdapter adapter = new CalendarAdapter(events);
        calendar_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        calendar_rv.setAdapter(adapter);


        Main.database.eventDao().getAllEvents().observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> eventList) {
                    events = eventList;
                    filterEventsByPicker(adapter);
            }
        });

        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> filterEventsByPicker(adapter));

        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> filterEventsByPicker(adapter));
    }

    private void filterEventsByPicker(CalendarAdapter adapter) {
        int selectedMonth = monthPicker.getValue();
        int selectedYear = yearPicker.getValue();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<Event> filteredEvents = filterEventsByMonthAndYear(events, selectedMonth, selectedYear);
            requireActivity().runOnUiThread(() -> adapter.updateEvents(filteredEvents));
        });
    }
    private List<Event> filterEventsByMonthAndYear(List<Event> events, int selectedMonth, int selectedYear) {
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            String date = event.getEventDate();
            String[] format = date.split("-");
            char ch=format[1].charAt(0);
            char str='1';
            if(ch=='0')
            {
                str=format[1].charAt(1);
                format[1]=String.valueOf(str);
            }
            int eventMonth = Integer.parseInt(format[1]) - 1;
            int eventYear = Integer.parseInt(format[0]);
            if (eventMonth == selectedMonth && eventYear == selectedYear) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }
}
