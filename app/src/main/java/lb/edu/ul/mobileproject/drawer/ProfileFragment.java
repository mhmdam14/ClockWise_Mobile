package lb.edu.ul.mobileproject.drawer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lb.edu.ul.mobileproject.CheckConnection;
import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.R;

public class ProfileFragment extends Fragment {

    private TextView userFullName;
    private EditText oldPassword, newPassword, confirmNewPassword;
    private String userEmailFromPrefs;
    private ImageButton camera;
    private ImageView profileImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences sharedPreferences;

    private final String API_BASE_URL = "http://"+ Main.ip+"/MobileDatabase";

    private static final String PROFILE_PREFS = "profile_prefs";
    private static final String PROFILE_IMAGE_KEY = "profile_image_path";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.profileImage);
        camera = view.findViewById(R.id.cameraAccess);
        userFullName = view.findViewById(R.id.userFullName);
        TextView userEmail = view.findViewById(R.id.userEmail);
        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);
        confirmNewPassword = view.findViewById(R.id.confirmNewPassword);
        TextView btnSavePassword = view.findViewById(R.id.btnSavePassword);
        View passwordForm = view.findViewById(R.id.passwordForm);
        View changePasswordSection = view.findViewById(R.id.changePasswordSection);

        sharedPreferences = requireContext().getSharedPreferences("session", Context.MODE_PRIVATE);
        userEmailFromPrefs = sharedPreferences.getString("email", null);
        if (userEmailFromPrefs != null) {
            userEmail.setText(userEmailFromPrefs);
            loadUserProfile(userEmailFromPrefs);
        } else {
            Toast.makeText(requireContext(), "No saved email found!", Toast.LENGTH_SHORT).show();
        }
        changePasswordSection.setOnClickListener(v -> {
            if (passwordForm.getVisibility() == View.VISIBLE) {
                passwordForm.setVisibility(View.GONE);
            } else {
                passwordForm.setVisibility(View.VISIBLE);
            }
        });
        btnSavePassword.setOnClickListener(v -> updatePassword());
        camera.setOnClickListener(v -> showPhotoDialog());
        loadSavedProfileImage();
    }
    private void loadUserProfile(String email) {
        String url = API_BASE_URL + "/getUser.php?email=" + email;
        sharedPreferences = requireContext().getSharedPreferences("fullName", Context.MODE_PRIVATE);

        if (!CheckConnection.isInternetConnection(requireContext())&&!sharedPreferences.getBoolean("got",false)) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                String fullName = jsonObject.getString("first_name") + " " + jsonObject.getString("last_name");
                                userFullName.setText(fullName);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("fullName", fullName);
                                editor.putBoolean("got",true);
                                editor.apply();
                            } else {
                                Toast.makeText(requireContext(), "Error fetching profile", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    },
                    error -> Toast.makeText(requireContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show());

            Volley.newRequestQueue(requireContext()).add(stringRequest);
        }
        else {
            userFullName.setText(sharedPreferences.getString("fullName"," "));
        }

    }
    private void updatePassword() {
        String oldPass = oldPassword.getText().toString();
        String newPass = newPassword.getText().toString();
        String confirmPass = confirmNewPassword.getText().toString();
        View passwordForm = requireView().findViewById(R.id.passwordForm); // Reference to the form
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            confirmNewPassword.setError("Passwords do not match!");
            return;
        }

        String url = API_BASE_URL + "/updatePassword.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            passwordForm.setVisibility(View.GONE);
                            oldPassword.setText("");
                            newPassword.setText("");
                            confirmNewPassword.setText("");
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        return;
                    }
                },
                error -> Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show())
        {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmailFromPrefs);
                params.put("old_password", oldPass);
                params.put("new_password", newPass);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    private void showPhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Profile Picture")
                .setItems(new String[]{"Upload Photo", "Delete Photo"}, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        deleteProfilePicture();
                    }
                })
                .show();
    }

    private void deleteProfilePicture() {
        SharedPreferences profilePrefs = requireContext().getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = profilePrefs.edit();
        editor.remove(PROFILE_IMAGE_KEY);
        editor.apply();
        profileImage.setImageDrawable(null);
        Toast.makeText(requireContext(), "Profile picture deleted!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("IntentReset")
    private void openImagePicker() {
        @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                    String imagePath = saveProfileImage(bitmap);
                    saveImagePathToPrefs(imagePath);
                    loadSavedProfileImage();
                } catch (IOException e) {
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveProfileImage(Bitmap bitmap) {
        try {
            File file = new File(requireContext().getFilesDir(), "profile.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveImagePathToPrefs(String imagePath) {
        if (imagePath != null) {
            SharedPreferences profilePrefs = requireContext().getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = profilePrefs.edit();
            editor.putString(PROFILE_IMAGE_KEY, imagePath);
            editor.apply();
        }
    }

    private void loadSavedProfileImage() {
        SharedPreferences profilePrefs = requireContext().getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE);
        String imagePath = profilePrefs.getString(PROFILE_IMAGE_KEY, null);

        if (imagePath != null) {
            File file = new File(imagePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Glide.with(this)
                        .load(bitmap)
                        .circleCrop()
                        .into(profileImage);
                camera.setBackgroundColor(Color.parseColor("#00000000"));
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) camera.getLayoutParams();
                params.rightMargin = -250;
                camera.setLayoutParams(params);
            }
        }
    }


}