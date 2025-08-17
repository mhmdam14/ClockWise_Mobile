package lb.edu.ul.mobileproject.calendar;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lb.edu.ul.mobileproject.AddEvent;
import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;
import lb.edu.ul.mobileproject.database.Event;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<Event> events;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public CalendarAdapter(List<Event> events) {
        this.events = events;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView calendar_day, name, time;
        private final ImageButton edit;
        private final ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            calendar_day = itemView.findViewById(R.id.calendar_day_id);
            name = itemView.findViewById(R.id.calendar_name_event_id);
            time = itemView.findViewById(R.id.calendar_time_event_id);
            edit = itemView.findViewById(R.id.calendar_edit);
            delete = itemView.findViewById(R.id.calendar_delete);
        }
    }

    @NonNull
    @Override
    public CalendarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_row, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CalendarAdapter.ViewHolder holder, int position) {
        Event currentEvent = events.get(position);
        String date = currentEvent.getEventDate();
        String[] format = date.split("-");
        String dayInNumber = format[2];
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dayOfWeek = "";
        try {
            Date date1 = sdf.parse(date);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
            dayOfWeek = dayFormat.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.calendar_day.setText(dayInNumber+"\n"+dayOfWeek);
        holder.name.setText(currentEvent.getEventName());
        holder.time.setText(currentEvent.getEventStartTime());

        // Delete button click listener
        holder.delete.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this event?")
                    .setIcon(R.drawable.warning)
                    .setPositiveButton("Yes", (dialog1, which) -> {
                        executorService.execute(() -> {
                            currentEvent.setSyncStatus("PENDING_DELETE");
                            Main.database.eventDao().update(currentEvent);

                            ((View) v.getParent()).post(() -> {
                                events.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, events.size());
                            });
                        });
                    })
                    .setNegativeButton("No", (dialog12, which) -> dialog12.dismiss())
                    .create();

            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.BLACK);
            }
        });

        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddEvent.class);
            intent.putExtra("id", currentEvent.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }
}
