package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.paillier.PaillierPublicKey;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SPAConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.volley.Request;
import org.telegram.messenger.volley.RequestQueue;
import org.telegram.messenger.volley.Response;
import org.telegram.messenger.volley.VolleyError;
import org.telegram.messenger.volley.toolbox.StringRequest;
import org.telegram.messenger.volley.toolbox.Volley;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by gzq on 16-1-12.
 */
public class SPARequest extends BaseFragment {

    private ListAdapter listAdapter;
    private String[] request;
    private String[] settings;
    private int[] settingsValues;
    private boolean[] clickValues;
    private String paillier_n;
    private String paillier_g;
    private String ope_key;
    private String requester;
    private String weight;
    private String spa_policies;
    private int settingSize;

    public SPARequest(Bundle args) {
        super(args);
    }
    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (arguments != null) {
            request = arguments.getString("request", "").split(" ");
            requester = request[0];
            weight = request[1];
            paillier_n = request[2];
            paillier_g = request[3];
            ope_key = request[4];
            settings = request[5].split(",");
            spa_policies = request[6];
            settingSize = settings.length;
            settingsValues = new int[settingSize];
            clickValues = new boolean[settingSize];
        } else {}

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }
    @Override
    public View createView(final Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SPARequest", R.string.SPAReceivedRequest));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(0xfff0f0f0);

        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDrawSelectorOnTop(true);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final long startTime = System.currentTimeMillis();
                for (int num = 0; num < 1; ++num) {
                if (i < 0 || i > settingSize || getParentActivity() == null) {
                    return;
                }
                if (i < settingSize) {
                    String setting = settings[i];
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle("Set value");
                    if (setting.compareTo("last_seen_setting") == 0) {
                        CharSequence[] items = new CharSequence[]{"Everybody", "My Contacts", "Nobody"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                // 001, 010, 100 for "Everybody", "My Contacts", "Nobody" respectively
                                settingsValues[i] = 0x1 << j;
                                clickValues[i] = true;
                            }
                        });
                        showDialog(builder.create());
                    } else if (setting.compareTo("passcode_lock_setting") == 0) {
                        CharSequence[] items = new CharSequence[]{"on", "off"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                // 01, 10 for "on", "off" respectively
                                settingsValues[i] = 0x1 << j;
                                clickValues[i] = true;
                            }
                        });
                        showDialog(builder.create());

                    } else if (setting.compareTo("average") == 0 || setting.compareTo("maximum_minimum_policy") == 0) {
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        numberPicker.setMinValue(10);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(15);
                        builder.setView(numberPicker);

                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                settingsValues[i] = numberPicker.getValue();
                                clickValues[i] = true;
                            }
                        });
                        showDialog(builder.create());
                    }
                } else if (i == settingSize) {
                    for (int j = 0; j < settingSize; ++j) {
                        if (!clickValues[j]) {
                            return;
                        }
                    }
                    String[] values = new String[settingSize];
                    PaillierPublicKey paillier = new PaillierPublicKey(new BigInteger(paillier_n),
                            new BigInteger(paillier_g));
                    final String req;


                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("average_policy", false);
                    for (int j = 0; j < settingSize; ++j) {
                        String setting = settings[j];
                        if (setting.compareTo("last_seen_setting") == 0) {
                            if (settingsValues[j] == 1) {
                                int weightInt = Integer.parseInt(weight);
                                values[j] = paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 1 * weightInt)).toString();
                            } else if (settingsValues[j] == 2) {
                                int weightInt = Integer.parseInt(weight);
                                values[j] = paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 1 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString();
                            } else if (settingsValues[j] == 4) {
                                int weightInt = Integer.parseInt(weight);
                                values[j] = paillier.encrypt(new BigInteger("" + 1 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString()
                                        + " " + paillier.encrypt(new BigInteger("" + 0 * weightInt)).toString();
                            }
                        } else if (setting.compareTo("passcode_lock_setting") == 0) {
                            if (settingsValues[j] == 1) {
                                int weightInt = Integer.parseInt(weight);
                                values[j] = paillier.encrypt(new BigInteger("" + 0 * weightInt))
                                        + " " + paillier.encrypt(new BigInteger("" + 1 * weightInt));
                            } else if (settingsValues[j] == 2) {
                                int weightInt = Integer.parseInt(weight);
                                values[j] = paillier.encrypt(new BigInteger("" + 1 * weightInt))
                                        + " " + paillier.encrypt(new BigInteger("" + 0 * weightInt));
                            }
                        } else if (setting.compareTo("average") == 0) {
                            int weightInt = Integer.parseInt(weight);
                            if (passcode_setting) {
                                values[j] = paillier.encrypt(new BigInteger("" + settingsValues[j])).toString()
                                        + " " + 1;
                            } else {
                                values[j] = paillier.encrypt(new BigInteger("" + settingsValues[j] * weightInt)).toString()
                                        + " " + weight;
                            }
                        } else if (setting.compareTo("maximum_minimum_policy") == 0) {
                            values[j] = paillier.encrypt(new BigInteger("" + settingsValues[j])).toString();
                        }
                    }

                    String localReq = values[0];
                    for (int j = 1; j < settingSize; ++j) {
                        localReq = localReq + "," + values[j];
                    }
                    req = localReq;

                    StringRequest stringRequest = new StringRequest(
                            Request.Method.POST,
                            SPAConfig.sendSPAResponse,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    CharSequence text = "Send request successfully";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();

                                    // SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                                    // SharedPreferences.Editor editor = preferences.edit();
                                    // // For test
                                    // editor.putStringSet("spa_request_poll_service", new TreeSet<String>());
                                    // editor.commit();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    CharSequence text = "Cannot connect server";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    Log.v("SPA", "SPA friend list activity cannot connect keymanager!");
                                }
                            }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            TLRPC.User user = UserConfig.getCurrentUser();
                            String respondent;
                            if (user != null && user.phone != null && user.phone.length() != 0) {
                                respondent = user.phone;
                            } else {
                                respondent = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                            }
                            params.put("values", req);
                            params.put("requester", requester);
                            params.put("respondent", respondent);
                            params.put("spa_policies", spa_policies);
                            params.put("paillier_n", paillier_n);
                            params.put("paillier_g", paillier_g);
                            params.put("settings", request[5]);
                            return params;
                        }
                    };

                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(stringRequest);

                }
                }
                final long endTime = System.currentTimeMillis();
                Log.v("spa", "time: " + (endTime - startTime));
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                // For test
                editor.putStringSet("spa_request_poll_service", new TreeSet<String>());
                editor.commit();
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i <= settingSize;
        }

        @Override
        public int getCount() {
            return settingSize + 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;

                String setting = settings[i];
                textCell.setText(setting, true);
            } else if (type == 1) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;

                textCell.setText("Send response", true);
            }

            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < settingSize) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
