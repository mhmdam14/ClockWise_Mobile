package lb.edu.ul.mobileproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Signup extends AppCompatActivity {
    EditText firstName, lastName, email, password, confirmPassword;
    String signupUrl = "http://"+Main.ip+"/MobileDatabase/signup.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firstName = findViewById(R.id.fname_id);
        lastName = findViewById(R.id.lname_id);
        email = findViewById(R.id.email_id);
        password = findViewById(R.id.password_id);
        confirmPassword = findViewById(R.id.confirm_password_id);
    }

    public void signupButtonHandler(View v) {
        String fname = firstName.getText().toString().trim();
        String lname = lastName.getText().toString().trim();
        String emailstr = email.getText().toString().trim();
        String passwordstr = password.getText().toString();
        String confirmPasswordstr = confirmPassword.getText().toString();

        if (fname.isEmpty() || lname.isEmpty() || emailstr.isEmpty() || passwordstr.isEmpty() || confirmPasswordstr.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!passwordstr.equals(confirmPasswordstr)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            new SignupTask().execute(fname, lname, emailstr, passwordstr);
        }
    }

    private class SignupTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String firstName = params[0];
            String lastName = params[1];
            String email = params[2];
            String password = params[3];

            try {
                URL url = new URL(signupUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "first_name=" + URLEncoder.encode(firstName, "UTF-8") +
                        "&last_name=" + URLEncoder.encode(lastName, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return result.toString();
            } catch (Exception e) {
                return "{\"error\":true,\"message\":\"Error: " + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject response = new JSONObject(result);
                if(response.getString("message").equals("Registration successful!")){
                    startActivity(new Intent(Signup.this,Login.class));
                }
            } catch (Exception e) {
                Toast.makeText(Signup.this, "Invalid server response", Toast.LENGTH_LONG).show();
            }
        }
    }
}