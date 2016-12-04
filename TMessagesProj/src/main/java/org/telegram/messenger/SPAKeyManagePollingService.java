package org.telegram.messenger;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.paillier.PaillierPrivateKey;
import org.telegram.messenger.volley.Request;
import org.telegram.messenger.volley.RequestQueue;
import org.telegram.messenger.volley.Response;
import org.telegram.messenger.volley.VolleyError;
import org.telegram.messenger.volley.toolbox.StringRequest;
import org.telegram.messenger.volley.toolbox.Volley;
import org.telegram.tgnet.TLRPC;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
                            if (response.compareTo("ok") != 0) {
                                if (response.startsWith("merged:")) {
                                    CharSequence text = "Get the merged result";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    // remove "merged:"
                                    String rawResult = response.substring(7);
                                    String[] results = rawResult.split(",");
                                    String[] policies =results[0].split(" ");
                                    String[] settings = results[1].split(" ");
                                    int valuesSize = results.length;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    PaillierPrivateKey paillierPrivateKey =
                                            new PaillierPrivateKey(new BigInteger(preferences.getString("paillier_lambda", "0")),
                                                    new BigInteger(preferences.getString("paillier_mu", "0")),
                                                    new BigInteger(preferences.getString("paillier_n", "0")));
                                    for (int i = 2, j = 0; i < valuesSize; ++i, ++j) {
                                        if (policies[j].compareTo("MajorityPreferred") == 0
                                                || policies[j].compareTo("MinorityPreferred") == 0) {
                                            String[] values = results[i].split(" ");
                                            if (settings[j].compareTo("last_seen_setting") == 0) {
                                                // If axay is neg number, the result is n + axay, which is a big number.
                                                BigInteger a1a2 = paillierPrivateKey.decrypt(new BigInteger(values[0]));
                                                BigInteger a1a3 = paillierPrivateKey.decrypt(new BigInteger(values[1]));
                                                BigInteger a2a3 = paillierPrivateKey.decrypt(new BigInteger(values[2]));
                                                if (a1a2.compareTo(BigInteger.ZERO) <= 0) {
                                                    if (a2a3.compareTo(BigInteger.ZERO) <= 0) {
                                                        editor.putInt(settings[j] + "_result", 3);
                                                    } else {
                                                        editor.putInt(settings[j] + "_result", 2);
                                                    }
                                                } else {
                                                    if (a1a3.compareTo(BigInteger.ZERO) <= 0) {
                                                        editor.putInt(settings[j] + "_result", 3);
                                                    } else {
                                                        editor.putInt(settings[j] + "_result", 1);
                                                    }
                                                }
                                            } else {
                                                BigInteger a1a2 = paillierPrivateKey.decrypt(new BigInteger(values[0]));
                                                if (a1a2.compareTo(BigInteger.ZERO) <= 0) {
                                                    editor.putInt(settings[j] + "_result", 2);
                                                } else {
                                                    editor.putInt(settings[j] + "_result", 1);
                                                }
                                            }
                                        } else {
                                            String[] values = results[i].split(" ");
                                            BigInteger value = paillierPrivateKey.decrypt(new BigInteger(values[0]));
                                            BigInteger weight = new BigInteger(values[1]);
                                            editor.putInt(settings[j] + "_result", value.divide(weight).intValue());
                                        }
                                    }
                                    editor.commit();
                                } else if (response.startsWith("result:")) {
                                    CharSequence text = "Get an SPA result, please go to SPA setting to assess it";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("spa_result_assessment", response.substring(7));
                                    editor.commit();
                                } else if (response.startsWith("assess:")) {
                                    String[] assessment = response.substring(7).split(",");
                                    CharSequence text = assessment[0] + " friends think result is suitable.\n"
                                            + assessment[1] + " friends think result is malicious.\n"
                                            + assessment[2] + " friends have no iead.";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                } else {
                                    CharSequence text = "Get an SPA request, please go to SPA setting to response it";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    Set<String> set = preferences.getStringSet("spa_request_poll_service", new TreeSet<String>());
                                    set.add(response);
                                    editor.putStringSet("spa_request_poll_service", set);
                                    editor.commit();
                                    Log.v("spa", "get keys: " + response);
                                }
                            } else {
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
