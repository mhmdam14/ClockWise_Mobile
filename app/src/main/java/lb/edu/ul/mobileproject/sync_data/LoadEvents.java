package lb.edu.ul.mobileproject.sync_data;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import lb.edu.ul.mobileproject.Main;
public class LoadEvents {
    Context context;
    String email;
    SharedPreferences sharedPreferences;

    public LoadEvents(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
    }

    public void loadFromDb(final VolleyCallback callback) {
        String url = "http://"+ Main.ip+"/MobileDatabase/getEvents.php";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccess,
                error -> callback.onFailure(error.getMessage()));

        requestQueue.add(jsonArrayRequest);
    }
}

