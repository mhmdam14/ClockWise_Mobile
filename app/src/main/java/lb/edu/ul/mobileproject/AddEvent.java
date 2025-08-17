package lb.edu.ul.mobileproject;

import static lb.edu.ul.mobileproject.Main.database;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lb.edu.ul.mobileproject.database.Event;
import lb.edu.ul.mobileproject.sync_data.StoreData;

public class AddEvent extends AppCompatActivity {
    private EditText eventNameEditText, eventDateEditText, startTimeEditText, finishTimeEditText, eventDescriptionEditText;
    int id;
    Event eventToEdit = null;
    Button submit_edit;
    SharedPreferences sessionSp;
    String email;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventNameEditText = findViewById(R.id.event_name_id);
        eventDateEditText = findViewById(R.id.event_date_id);
        startTimeEditText = findViewById(R.id.start_time_id);
        finishTimeEditText = findViewById(R.id.finish_time_id);
        eventDescriptionEditText = findViewById(R.id.event_description_id);
        submit_edit = findViewById(R.id.add_event_button_id);

        sessionSp = getSharedPreferences("session", MODE_PRIVATE);
        email = sessionSp.getString("email", "");
        eventDateEditText.setOnClickListener(v -> showDatePicker());
        startTimeEditText.setOnClickListener(this::showTimePicker);
        finishTimeEditText.setOnClickListener(this::showTimePicker);

        id = getIntent().getIntExtra("id", -1);
        if (id != -1) {
            submit_edit.setText("Edit");
            loadEventData(id);
        }
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDatePicker,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String strDay = selectedDay < 10 ? "0" + selectedDay : String.valueOf(selectedDay);
                    String strMonth = selectedMonth + 1 < 10 ? "0" + (selectedMonth + 1) : String.valueOf(selectedMonth + 1);
                    String date = selectedYear + "-" + strMonth + "-" + strDay;
                    eventDateEditText.setText(date);
                }, year, month, day);

        datePickerDialog.setOnShowListener(dialog -> {
            Button okButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button cancelButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);

            okButton.setTextColor(Color.BLACK);
            cancelButton.setTextColor(Color.BLACK);
        });

        datePickerDialog.show();
    }

    private void showTimePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.CustomTimePicker,
                (timePicker, selectedHour, selectedMinute) -> {
                    @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    if (view.getId() == R.id.start_time_id) {
                        startTimeEditText.setText(time);
                    } else if (view.getId() == R.id.finish_time_id) {
                        finishTimeEditText.setText(time);
                    }
                }, hour, minute, true);
        timePickerDialog.setOnShowListener(dialog -> {
            Button okButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE);
            Button cancelButton = timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE);

            okButton.setTextColor(Color.BLACK);
            cancelButton.setTextColor(Color.BLACK);
        });

        timePickerDialog.show();
    }

    public void submitEventButtonHandler(View v) {
        String eventName = eventNameEditText.getText().toString();
        String eventDate = eventDateEditText.getText().toString();
        String startTime = startTimeEditText.getText().toString();
        String finishTime = finishTimeEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();

        if (eventName.isEmpty()) {
            eventNameEditText.setError("Can't be empty!");
            return;
        } else if (eventDescription.isEmpty()) {
            eventDescriptionEditText.setError("Can't be empty!");
            return;
        } else if (eventDate.isEmpty()) {
            eventDateEditText.setError("Can't be empty!");
            return;
        } else if (startTime.isEmpty()) {
            startTimeEditText.setError("Can't be empty!");
            return;
        } else if (finishTime.isEmpty()) {
            finishTimeEditText.setError("Can't be empty!");
            return;
        }

        Event event = new Event();
        event.setEventName(eventName);
        event.setEventDescription(eventDescription);
        event.setEventDate(eventDate);
        event.setEventStartTime(startTime);
        event.setEventFinishTime(finishTime);
        event.setEventDateTime(eventDate+" "+startTime);

        if (id != -1) {
            executorService.execute(() -> {
                Event event1 = database.eventDao().getEventById(id);
                if (event1 != null) {
                    event.setSyncStatus("PENDING_EDIT/" +
                            event1.getEventName() + "/" + eventName + "/" +
                            event1.getEventDescription() + "/" + eventDescription + "/" +
                            event1.getEventDate() + "/" + eventDate + "/" +
                            event1.getEventStartTime() + "/" + startTime + "/" +
                            event1.getEventFinishTime() + "/" + finishTime);

                    event.setId(id);
                    updateEventInBackground(event);
                }
            });

        } else {
            event.setSyncStatus("PENDING_ADD");
            insertEventInBackground(event);
            if (!CheckConnection.isInternetConnection(this)) {
                StoreData storeData = new StoreData(this);
                storeData.storeInDb();
            }
        }
    }

    public void loadEventData(int id) {
        executorService.execute(() -> {
            eventToEdit = database.eventDao().getEventById(id);
            if (eventToEdit != null) {
                runOnUiThread(() -> {
                    eventNameEditText.setText(eventToEdit.getEventName());
                    eventDescriptionEditText.setText(eventToEdit.getEventDescription());
                    eventDateEditText.setText(eventToEdit.getEventDate());
                    startTimeEditText.setText(eventToEdit.getEventStartTime());
                    finishTimeEditText.setText(eventToEdit.getEventFinishTime());
                });
            }
        });
    }


    public void updateEventInBackground(Event event) {
        executorService.execute(() -> {
            database.eventDao().update(event);
            runOnUiThread(this::finish);
        });
    }

    public void insertEventInBackground(Event event) {
        executorService.execute(() -> {
            database.eventDao().insertAll(event);
            Log.d("AddEvent", "Event inserted");
            runOnUiThread(this::finish);
        });
    }
}
