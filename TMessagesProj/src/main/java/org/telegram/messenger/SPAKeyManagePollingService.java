package org.telegram.messenger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.telegram.messenger.volley.Request;
import org.telegram.messenger.volley.RequestQueue;
import org.telegram.messenger.volley.Response;
import org.telegram.messenger.volley.VolleyError;
import org.telegram.messenger.volley.toolbox.StringRequest;
import org.telegram.messenger.volley.toolbox.Volley;
import org.telegram.tgnet.TLRPC;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gzq on 15-11-24.
 */
public class SPAKeyManagePollingService extends Service {

    Context context = this;

    public static final String ACTION = "org.telegram.android.spa.Key_Manager";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new PollingThread().start();
        return super.onStartCommand(intent, flags, startId);
    }

    class PollingThread extends Thread {
        @Override
        public void run() {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = SPAConfig.pollingKeyManager;
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.compareTo("ok") == 0) {
                            } else {
                                Log.v("spa", "get keys: " + response);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("SPA", "SPAKeyManagePollingService didn't work!");
                }
            }) {
                protected Map<String, String> getParams() {
                    TLRPC.User user = UserConfig.getCurrentUser();
                    String value;
                    if (user != null && user.phone != null && user.phone.length() != 0) {
                        value = user.phone;
                    } else {
                        value = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                    }
                    Map<String, String> params = new HashMap<>();
                    params.put("id", value);
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("SPAKeyManagePollingService: onDestroy");
    }

}
