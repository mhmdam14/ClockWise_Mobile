package lb.edu.ul.mobileproject.sync_data;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lb.edu.ul.mobileproject.Main;
import lb.edu.ul.mobileproject.database.Event;

public class StoreData {
    Context context;
    SharedPreferences sharedPreferences;
    String email;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public StoreData(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");
    }

    public void storeInDb() {
        LoadEvents loadEvents = new LoadEvents(context);
        loadEvents.loadFromDb(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                try {

                    for (int i = 0; i < result.length(); i++) {
                        JSONObject remoteEvent = result.getJSONObject(i);
                        if ("PENDING_ADD".equals(remoteEvent.getString("syncStatus"))){
                            sendEventToRoom(remoteEvent);
                        }

                        else if("PENDING_EDIT".equals(remoteEvent.getString("syncStatus").toUpperCase().split("/")[0])){
                            editEventInRoom(remoteEvent);
                        }
                        else if("PENDING_DELETE".equals(remoteEvent.getString("syncStatus"))){
                            deleteEventFromRoom(remoteEvent);
                        }
                    }
                } catch (Exception e) {
                    return;
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Failed to store in Local!", Toast.LENGTH_SHORT).show();
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Event> localEvents = Main.database.eventDao().getListOfEvents();
                for (Event event : localEvents) {
                    if ("PENDING_ADD".equals(event.getSyncStatus())) {
                        sendEventToServer(event);
                    }
                    else if("PENDING_DELETE".equals(event.getSyncStatus())){
                        deleteEventFromServer(event);
                    }
                    else if("PENDING_EDIT".equals(event.getSyncStatus().toUpperCase().split("/")[0])){
                        editEventInServer(event);
                    }
                }
            }
        });
    }
    private void sendEventToRoom(JSONObject remoteEvent){
        executorService.execute(new Runnable() {
            List<Event> localEvents;
            @Override
            public void run() {
                localEvents = Main.database.eventDao().getListOfEvents();
                try {
                    String startTime = remoteEvent.getString("eventStartTime");
                    String[] format = startTime.split(":");
                    startTime = format[0] + ":" + format[1];

                    String finishTime = remoteEvent.getString("eventFinishTime");
                    format = finishTime.split(":");
                    finishTime = format[0] + ":" + format[1];

                    Event event = new Event();
                    event.setEventName(remoteEvent.getString("eventName"));
                    event.setEventDescription(remoteEvent.getString("eventDescription"));
                    event.setEventDate(remoteEvent.getString("eventDate"));
                    event.setEventStartTime(startTime);
                    event.setEventFinishTime(finishTime);
                    event.setEventDateTime(remoteEvent.getString("eventDate")+" "+startTime);
                    event.setSyncStatus("SYNCED");
                    Main.database.eventDao().insertAll(event);


                    event.setSyncStatus("PENDING_EDIT/"+event.getEventName()+"/"+event.getEventName()+"/"+
                            event.getEventDescription()+"/"+event.getEventDescription()+"/"+
                            event.getEventDate()+"/"+event.getEventDate()+"/"+
                            event.getEventStartTime()+"/"+event.getEventStartTime()+"/"+
                            event.getEventFinishTime()+"/"+event.getEventFinishTime()+"/"+
                            "SYNCED");
                    editEventInServer(event);


                }catch (Exception e){
                    return;
                }
            }
        });
    }

    private void editEventInRoom(JSONObject remoteEvent){
                try {
                    executorService.execute(new Runnable() {
                        final String sync = remoteEvent.getString("syncStatus");
                        final String[] format = sync.split("/");
                        final String[] format1 = format[7].split(":");
                        final String startTime = format1[0] + ":" + format1[1];
                        final String[] format3 = format[9].split(":");
                        final String finishTime = format3[0] + ":" + format3[1];
                        @Override
                        public void run() {
                            Event eventToUpdate = Main.database.eventDao().getEventByALL(format[1], format[3], format[5], startTime, finishTime);
                            if(eventToUpdate!=null) {
                                eventToUpdate.setEventName(format[2]);
                                eventToUpdate.setEventDescription(format[4]);
                                eventToUpdate.setEventDate(format[6]);
                                String[] format2 = format[8].split(":");
                                eventToUpdate.setEventStartTime(format2[0] + ":" + format2[1]);
                                format2 = format[10].split(":");
                                eventToUpdate.setEventFinishTime(format2[0] + ":" + format2[1]);
                                eventToUpdate.setSyncStatus("SYNCED");
                                Main.database.eventDao().update(eventToUpdate);
                            }
                        }
                    });
                    String url="http://"+ Main.ip+"/MobileDataBase/syncEvent.php" +
                            "?userEmail="+email+
                            "&eventName="+remoteEvent.getString("eventName")+
                            "&eventDescription="+remoteEvent.getString("eventDescription")+
                            "&eventDate="+remoteEvent.getString("eventDate")+
                            "&eventStartTime="+remoteEvent.getString("eventStartTime")+
                            "&eventFinishTime="+remoteEvent.getString("eventFinishTime");
                    RequestQueue requestQueue=Volley.newRequestQueue(context);
                    StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    }, error -> {
                    });
                    requestQueue.add(stringRequest);

                } catch (Exception e) {
                    return;
                }
    }


    private void deleteEventFromRoom(JSONObject remoteEvent){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String eventName = remoteEvent.getString("eventName");
                    String eventDate =remoteEvent.getString("eventDate");
                    String eventDescription=remoteEvent.getString("eventDescription");
                    String eventStartTime=remoteEvent.getString("eventStartTime");
                    String eventFinishTime=remoteEvent.getString("eventFinishTime");

                    String[] format= eventStartTime.split(":");
                    eventStartTime = format[0] + ":" + format[1];

                    format = eventFinishTime.split(":");
                    eventFinishTime = format[0] + ":" + format[1];


                    Event localEvent = Main.database.eventDao().getEventByALL(eventName,eventDescription,eventDate,eventStartTime,eventFinishTime);

                    if (localEvent != null) {
                        Main.database.eventDao().delete(localEvent);}

                    String url = "http://"+ Main.ip+"/MobileDatabase/deleteEvents.php?userEmail=" +email;
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(stringRequest);

                } catch (Exception e) {
                    return;
                }
            }
        });
    }

    private void sendEventToServer(final Event event) {
        String url = "http://"+ Main.ip+"/MobileDatabase/addEvent.php?userEmail=" + email
                + "&eventName=" + Uri.encode(event.getEventName())
                + "&eventDescription=" + Uri.encode(event.getEventDescription())
                + "&eventDate=" + Uri.encode(event.getEventDate())
                + "&eventStartTime=" + Uri.encode(event.getEventStartTime())
                + "&eventFinishTime=" + Uri.encode(event.getEventFinishTime())
                + "&syncStatus=" + Uri.encode("SYNCED");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateSyncStatus(event);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void updateSyncStatus(final Event event) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                event.setSyncStatus("SYNCED");
                Main.database.eventDao().update(event);
            }
        });
    }

    private void editEventInServer(final Event event){
        String url = "http://"+ Main.ip+"/MobileDatabase/editEvent.php?userEmail=" + email+"&syncStatus="+event.getSyncStatus();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                    event.setSyncStatus("SYNCED");
                                    Main.database.eventDao().update(event);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void deleteEventFromServer(final Event event){
        String eventStartTime = event.getEventStartTime() + ":00";
        String eventFinishTime = event.getEventFinishTime() + ":00";

        String url = "http://" + Main.ip + "/MobileDatabase/deleteEvent.php?userEmail=" + email+
                "&eventName=" + event.getEventName() + "&eventDescription=" + event.getEventDescription() + "&eventDate=" +event.getEventDate()+
                "&eventStartTime=" + eventStartTime + "&eventFinishTime=" +eventFinishTime;


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                Main.database.eventDao().delete(event);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


}