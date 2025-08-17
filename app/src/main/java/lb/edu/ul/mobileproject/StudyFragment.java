package lb.edu.ul.mobileproject;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class StudyFragment extends Fragment {

    private TextView timerText;
    private Button stopButton;

    private ImageView icon2;
    private boolean isTimerRunning = false;
    private long timeRemaining = 1500000;
    private Handler handler;
    private Runnable updateTimerRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_study, container, false);

        timerText = rootView.findViewById(R.id.timer_text);
        stopButton = rootView.findViewById(R.id.stop_button);
        icon2 = rootView.findViewById(R.id.icon_2);

        stopButton.setText("Start Studying");
        updateTimerDisplay(); // Set initial timer display

        stopButton.setOnClickListener(v -> toggleTimer());
        icon2.setOnClickListener(v -> toggleTimer());

        handler = new Handler();
        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    if (timeRemaining > 0) {
                        timeRemaining -= 1000; // Decrease time by 1 second
                        updateTimerDisplay();
                        handler.postDelayed(this, 1000);
                    } else {
                        stopTimer(); // Stop when reaching 00:00
                    }
                }
            }
        };

        return rootView;
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isTimerRunning = true;
        handler.post(updateTimerRunnable);
        stopButton.setText("Stop Studying");
        icon2.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void stopTimer() {
        isTimerRunning = false;
        handler.removeCallbacks(updateTimerRunnable);
        stopButton.setText("Start Studying");
        icon2.setImageResource(android.R.drawable.ic_media_play);
    }

    private void updateTimerDisplay() {
        long seconds = timeRemaining / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateTimerRunnable);
    }
}