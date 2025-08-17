package lb.edu.ul.mobileproject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Login extends AppCompatActivity {
    EditText EmailInput;
    EditText PassInput;
    String email, pass;
    CheckBox rememberMe;
    SharedPreferences sharedPreferences,sessionSp;

    String loginUrl = "http://"+ Main.ip+"/MobileDatabase/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rememberMe = findViewById(R.id.remember_me_id);
        EmailInput = findViewById(R.id.login_email_id);
        PassInput = findViewById(R.id.login_password_id);
        sharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE);
        sessionSp=getSharedPreferences("session",MODE_PRIVATE);
        boolean rememberMeStatus = sharedPreferences.getBoolean("rememberMe", false);
        email = sharedPreferences.getString("email", "");
        pass = sharedPreferences.getString("password", "");

        if(CheckConnection.isInternetConnection(this))
        {
            if (rememberMeStatus && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                rememberMe.setChecked(true);
                startActivity(new Intent(this, Main.class));
            }
        }
        else{
            if (rememberMeStatus && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                rememberMe.setChecked(true);
                new LoginTask().execute(email, pass);
            }
        }

    }

    public void loginButtonHandler(View v) {
        email = EmailInput.getText().toString();
        pass = PassInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            EmailInput.setError("Empty field!");
            return;
        }
         if (TextUtils.isEmpty(pass)) {
            PassInput.setError("Empty field!");
            return;
        }
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";
        if (!email.matches(emailPattern)) {
            EmailInput.setError("Invalid email format!");
            return;
        }
        new LoginTask().execute(email, pass);
    }

    private String checkUserCredentials() {
        try {
            URL url = new URL(loginUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

            String postData = "email=" + URLEncoder.encode(email, "UTF-8") +
                    "&password=" + URLEncoder.encode(pass, "UTF-8");

            OutputStream os = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bufferedWriter.write(postData);
            bufferedWriter.flush();
            bufferedWriter.close();
            os.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            return stringBuilder.toString();

        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"Error: " + e.getMessage() + "\"}";
        }
    }
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            email = strings[0];
            pass = strings[1];
            return checkUserCredentials();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                Toast.makeText(Login.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                if (jsonObject.getString("message").equals("Logged in successfully!")) {
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    if (rememberMe.isChecked()) {
                        ed.putString("email", email);
                        ed.putString("password", pass);
                        ed.putBoolean("rememberMe", true);
                    } else {
                        ed.putString("email", "");
                        ed.putString("password", "");
                        ed.putBoolean("rememberMe", false);
                    }
                    ed.apply();
                    SharedPreferences.Editor ed1= sessionSp.edit();
                    ed1.putString("email",email);
                    ed1.apply();
                    startActivity(new Intent(Login.this, Main.class));
                }
            } catch (Exception e) {
                Toast.makeText(Login.this, "Invalid server response", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void sendToSignupHandler(View v) {
        Intent sendToSignup = new Intent(this, Signup.class);
        startActivity(sendToSignup);
    }
}
