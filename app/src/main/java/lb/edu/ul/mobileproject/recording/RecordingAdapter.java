package lb.edu.ul.mobileproject.recording;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;
import lb.edu.ul.mobileproject.database.Recording;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordingViewHolder> {

    private final Context context;
    private List<Recording> recordings = new ArrayList<>();
    private final MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;
    private int lastPlaybackPosition = 0;
    private final SeekBarUpdater seekBarUpdater;
    private LifecycleOwner lifecycleOwner;

    public RecordingAdapter(Context context) {
        this.context = context;
        this.mediaPlayer = new MediaPlayer();
        this.seekBarUpdater = new SeekBarUpdater();
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recordings_row, parent, false);
        return new RecordingViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        Recording recording = recordings.get(position);
        holder.recordingName.setText(recording.getFileName());

        // Play/Pause Button
        holder.btnPlayPause.setOnClickListener(v -> {
            if (currentlyPlayingPosition == position) {
                if (mediaPlayer.isPlaying()) {
                    pauseAudio(holder);
                } else {
                    resumeAudio(holder);
                }
            } else {
                playAudio(recording.getFilePath(), holder, position);
            }
        });

        // Rewind Button (5 sec back)
        holder.btnRewind.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(Math.max(mediaPlayer.getCurrentPosition() - 5000, 0));
            }
        });

        // Forward Button (5 sec forward)
        holder.btnForward.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(Math.min(mediaPlayer.getCurrentPosition() + 5000, mediaPlayer.getDuration()));
            }
        });

        // Delete Recording
        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this recording?")
                    .setIcon(R.drawable.warning)
                    .setPositiveButton("Yes", (dialog1, which) -> {
                        stopCurrentPlayback(); // Stop playing before deleting
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.execute(() -> Main.database.recordingDao().delete(recording));
                        dialog1.dismiss();
                    })
                    .setNegativeButton("No", (dialog12, which) -> dialog12.dismiss())
                    .create();

            dialog.show();

            // Change message text color
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.BLACK);
            }
        });

        // SeekBar Listener
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo((progress * mediaPlayer.getDuration()) / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update UI based on playback state
        if (position == currentlyPlayingPosition && mediaPlayer.isPlaying()) {
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.seekBar.setProgress(0);
            holder.tvCurrentTime.setText("00:00");
            holder.tvTotalTime.setText("00:00");
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }

        if (lifecycleOwner != null) {
            lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                public void onStop() {
                    if (mediaPlayer.isPlaying()) {
                        pauseAudio(holder);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecordings(List<Recording> newRecordings) {
        this.recordings = newRecordings;
        notifyDataSetChanged();
    }

    private void playAudio(String filePath, RecordingViewHolder holder, int position) {
        stopCurrentPlayback();

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(filePath));
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentlyPlayingPosition = position;
            lastPlaybackPosition = 0;
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            holder.tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));

            seekBarUpdater.setHolder(holder);
            seekBarUpdater.run();

            mediaPlayer.setOnCompletionListener(mp -> stopCurrentPlayback());

        } catch (IOException e) {
            Log.e("MediaPlayer", "Error playing audio: " + e.getMessage());
            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudio(RecordingViewHolder holder) {
        if (mediaPlayer.isPlaying()) {
            lastPlaybackPosition = mediaPlayer.getCurrentPosition(); // Save current position
            mediaPlayer.pause();
            holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void resumeAudio(RecordingViewHolder holder) {
        mediaPlayer.start();
        mediaPlayer.seekTo(lastPlaybackPosition); // Resume from last position
        holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        seekBarUpdater.setHolder(holder);
        seekBarUpdater.run();
    }

    private void stopCurrentPlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        lastPlaybackPosition = 0;
        currentlyPlayingPosition = -1;
    }

    private class SeekBarUpdater implements Runnable {
        private RecordingViewHolder holder;

        void setHolder(RecordingViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            if (mediaPlayer.isPlaying() && holder != null) {
                holder.seekBar.setProgress((mediaPlayer.getCurrentPosition() * 100) / mediaPlayer.getDuration());
                holder.tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                holder.seekBar.postDelayed(this, 1000);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int millis) {
        return String.format("%02d:%02d", (millis / 1000) / 60, (millis / 1000) % 60);
    }

    public static class RecordingViewHolder extends RecyclerView.ViewHolder {
        TextView recordingName, tvCurrentTime, tvTotalTime;
        SeekBar seekBar;
        ImageButton btnPlayPause, btnRewind, btnForward, btnDelete;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingName = itemView.findViewById(R.id.recordingName);
            seekBar = itemView.findViewById(R.id.seekBar);
            btnPlayPause = itemView.findViewById(R.id.btn_play_pause);
            btnRewind = itemView.findViewById(R.id.btn_rewind);
            btnForward = itemView.findViewById(R.id.btn_forward);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvCurrentTime = itemView.findViewById(R.id.tv_current_time);
            tvTotalTime = itemView.findViewById(R.id.tv_total_time);
        }
    }
}
