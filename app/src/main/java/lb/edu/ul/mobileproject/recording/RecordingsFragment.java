package lb.edu.ul.mobileproject.recording;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;
import lb.edu.ul.mobileproject.database.AppDatabase;
import lb.edu.ul.mobileproject.database.Recording;
import lb.edu.ul.mobileproject.database.RecordingDao;

public class RecordingsFragment extends Fragment {

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;
    private RecordingDao recordingDao;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordings, container, false);

        Button uploadButton = view.findViewById(R.id.btn_upload_recording_id);
        recyclerView = view.findViewById(R.id.recyclerView_recordings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AppDatabase database = Main.database;
        recordingDao = database.recordingDao();

        adapter = new RecordingAdapter(getContext());
        recyclerView.setAdapter(adapter);
        adapter.setLifecycleOwner(getViewLifecycleOwner());

        LiveData<List<Recording>> liveRecordings = recordingDao.getAllRecordings();
        liveRecordings.observe(getViewLifecycleOwner(), new Observer<List<Recording>>() {
            @Override
            public void onChanged(List<Recording> recordings) {
                adapter.setRecordings(recordings);
            }
        });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedAudioUri = result.getData().getData();
                        if (selectedAudioUri != null) {
                            saveRecording(selectedAudioUri);
                        }
                    }
                }
        );

        uploadButton.setOnClickListener(v -> checkPermissionAndOpenFilePicker());

        return view;
    }
    private void checkPermissionAndOpenFilePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }

    private void saveRecording(Uri audioUri) {
        String fileName = "Audio " + System.currentTimeMillis();

        requireActivity().getContentResolver().takePersistableUriPermission(
                audioUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        Recording recording = new Recording(fileName, audioUri.toString());
        new Thread(() -> recordingDao.insert(recording)).start();
        Toast.makeText(getContext(), "Recording saved", Toast.LENGTH_SHORT).show();
    }

}