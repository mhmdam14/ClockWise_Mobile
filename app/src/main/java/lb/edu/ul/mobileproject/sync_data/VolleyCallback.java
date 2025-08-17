package lb.edu.ul.mobileproject.sync_data;

import org.json.JSONArray;

public interface VolleyCallback {
    void onSuccess(JSONArray result);
    void onFailure(String errorMessage);
}
