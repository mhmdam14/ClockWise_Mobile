package lb.edu.ul.mobileproject.home;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lb.edu.ul.mobileproject.AddEvent;
import lb.edu.ul.mobileproject.R;
import lb.edu.ul.mobileproject.database.Event;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    List<Event> events;
    public HomeAdapter(List<Event> events) {
        this.events = filterEventsForToday(events);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventTime;
        private final TextView eventDescription;
        private final TextView eventTimeout;
        private final Button eventButton;
        private final ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTime = itemView.findViewById(R.id.time_id);
            eventDescription = itemView.findViewById(R.id.event_description_id);
            eventTimeout = itemView.findViewById(R.id.event_timeout_id);
            eventButton = itemView.findViewById(R.id.event_name_id);
            editButton=itemView.findViewById(R.id.event_edit);
        }

        public TextView getTimeTextView() {
            return eventTime;
        }

        public Button getEventNameButton() {
            return eventButton;
        }
        public ImageButton getEditButton() {
            return editButton;
        }

        public TextView getEventDescriptionTextView() {
            return eventDescription;
        }

        public TextView getEventTimeoutTextView() {
            return eventTimeout;
        }

    }
    private String getEventDuration(String startTime, String finishTime) {
        if (startTime == null || finishTime == null || startTime.isEmpty() || finishTime.isEmpty()) {
            return "Invalid time";
        }
        int startMinutes = parseTimeToMinutes(startTime);
        int finishMinutes = parseTimeToMinutes(finishTime);

        if (startMinutes == -1 || finishMinutes == -1) {
            return "Invalid time format";
        }

        int duration = finishMinutes - startMinutes;

        if (duration < 0) {
            return "End time before start time";
        }

        int hours = duration / 60;
        int minutes = duration % 60;

        String durationString = hours > 0 ? hours + " hr" : "";
        if (minutes > 0) {
            // If hours are present, append "and", otherwise just show minutes
            if (!durationString.isEmpty()) {
                durationString += " and ";
            }
            durationString += minutes + " min" + (minutes > 1 ? "s" : "");
        }

        return durationString;
    }

    // Utility method to parse HH:mm time format to minutes
    private int parseTimeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return -1;  // Return -1 for invalid time format
            }
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Check if the hours and minutes are within valid ranges
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return -1;  // Return -1 if the time is out of bounds
            }

            return hours * 60 + minutes;
        } catch (Exception e) {
            return -1;
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event currentEvent = events.get(position);
        holder.getTimeTextView().setText(currentEvent.getEventStartTime());
        holder.getEventNameButton().setText(currentEvent.getEventName());
        holder.getEventDescriptionTextView().setText(currentEvent.getEventDescription());
        String eventDuration = getEventDuration(currentEvent.getEventStartTime(), currentEvent.getEventFinishTime());
        holder.getEventTimeoutTextView().setText(eventDuration);
        holder.getEditButton().setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AddEvent.class);
            intent.putExtra("id", currentEvent.getId());
            holder.itemView.getContext().startActivity(intent);
        });


        holder.itemView.setVisibility(View.VISIBLE);
    }


    @Override
    public int getItemCount() {
        return events.size();
    }
    private List<Event> filterEventsForToday(List<Event> events) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            String eventDate = event.getEventDate();
            if (currentDate.equals(eventDate)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }
}