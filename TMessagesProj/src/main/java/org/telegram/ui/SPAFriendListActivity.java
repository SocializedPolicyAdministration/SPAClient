package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.paillier.PaillierPublicKey;
import org.telegram.PhoneFormat.PhoneFormat;
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
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqguo on 2015/10/28.
 */
public class SPAFriendListActivity extends BaseFragment implements ContactsActivity.ContactsActivityDelegate {
    private class SPAUser {
        int userId;
        int weight;
        SPAUser(int userId, int weight) {
            if (weight != UNPROFESSIONAL_USER || weight != PROFESSIONAL_USER) {
                weight = UNPROFESSIONAL_USER;
            }
            this.userId = userId;
            this.weight = weight;
        }
    }

    private final int UNPROFESSIONAL_USER = 1;
    private final int PROFESSIONAL_USER = 2;

    private ListView listView;
    private ListAdapter listViewAdapter;
    private TextView emptyTextView;
    private FrameLayout progressView;
    private int selectedUserId;
    private ArrayList<SPAUser> usersId = new ArrayList<>();

    // in order to get users' phone number for `sendSPARequest`
    private ArrayList<String[]> usersPhoneAndWeight = new ArrayList<>();

    private final int leastNumberForSendSPARequest = 1;

    private final static int invite_friends = 1;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(final Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SPAFriendsList", R.string.SPAFriendsList));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == invite_friends) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlyUsers", true);
                    args.putBoolean("destroyAfterSelect", true);
                    args.putBoolean("returnAsResult", true);
                    ContactsActivity fragment = new ContactsActivity(args);
                    fragment.setDelegate(SPAFriendListActivity.this);
                    presentFragment(fragment);
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(invite_friends, R.drawable.plus);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyTextView = new TextView(context);
        emptyTextView.setTextColor(0xff808080);
        emptyTextView.setTextSize(20);
        emptyTextView.setGravity(Gravity.CENTER);
        emptyTextView.setVisibility(View.INVISIBLE);
        emptyTextView.setText(LocaleController.getString("NoSelected", R.string.NoSelected));
        frameLayout.addView(emptyTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        emptyTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        progressView = new FrameLayout(context);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        ProgressBar progressBar = new ProgressBar(context);
        progressView.addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        listView = new ListView(context);
        listView.setEmptyView(emptyTextView);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setAdapter(listViewAdapter = new ListAdapter(context));
        if (Build.VERSION.SDK_INT >= 11) {
            listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int usersSize = usersId.size();
                if (i < usersSize) {
                    Bundle args = new Bundle();
                    args.putInt("user_id", usersId.get(i).userId);
                    presentFragment(new ProfileActivity(args));
                } else if (i == usersSize) {
                    // pass
                } else if (i > usersSize) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean containsLastSeen = preferences.contains("last_seen_setting");
                    boolean containsPasscodeLock = preferences.contains("passcode_lock_setting");
                    boolean containsAverage = preferences.contains("average_policy");
                    boolean containsMinMax = preferences.contains("maximum_minimum_policy");
                    if (usersSize >= leastNumberForSendSPARequest &&
                            (containsLastSeen || containsPasscodeLock
                            || containsAverage || containsMinMax)) {
                        if (sendSPARequest(containsLastSeen, containsPasscodeLock,
                                containsAverage, containsMinMax, context)) {
                            // TODO: 15-11-25 Toast success
                        } else {
                            // TODO: 15-11-25 Toast fail
                        }
                    } else {
                        // TODO: 15-11-25 Toast no setting be chosen to send request
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 0 || i >= usersId.size() || getParentActivity() == null) {
                    return true;
                }
                selectedUserId = usersId.get(i).userId;
                final int selectedItem = i;

                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                CharSequence[] items = new CharSequence[]{"UnProfessional user", "Professional user", LocaleController.getString("Deselect", R.string.Deselect)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            usersId.get(selectedItem).weight = 1;
                        } else if (i == 1) {
                            usersId.get(selectedItem).weight = 2;
                        } else if (i == 2) {
                            usersId.remove(selectedItem);
                            listViewAdapter.notifyDataSetChanged();
                        }
                    }
                });
                showDialog(builder.create());

                return true;
            }
        });

        if (!usersId.isEmpty()) {
            progressView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            listView.setEmptyView(null);
        } else {
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyTextView);
        }

        return fragmentView;
    }

    private boolean sendSPARequest(boolean containsLastSeen, boolean containsPasscodeLock,
                                   boolean containsAverage, boolean containsMinMax, final Context context) {
        SharedPreferences preferences =
                ApplicationLoader. applicationContext.
                        getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);

        // generate json request
        RequestQueue queue = Volley.newRequestQueue(context);
        ArrayList<String> settings = new ArrayList<>();
        ArrayList<String> policies = new ArrayList<>();
        ArrayList<String> respondentsId = new ArrayList<>();
        ArrayList<String> respondentsWeight = new ArrayList<>();

        if (containsLastSeen) {
            settings.add("last_seen_setting");
            policies.add(preferences.getString("last_seen_setting_policy", "MajorityPreferred"));
        }
        if (containsPasscodeLock) {
            settings.add("passcode_lock_setting");
            policies.add(preferences.getString("passcode_lock_setting_policy", "MajorityPreferred"));
        }
        if (containsAverage) {
            settings.add("average");
            policies.add("Average");
        }
        if (containsMinMax) {
            settings.add("maximum_minimum_policy");
            policies.add(preferences.getString("maximum_minimum_policy_policy", "MaximumValue"));
        }
        TLRPC.User user = UserConfig.getCurrentUser();
        int respondentsSize = usersPhoneAndWeight.size();
        String paillierN = preferences.getString("paillier_n", "1");
        String paillierG = preferences.getString("paillier_g", "1");
        String opeK = preferences.getString("ope_key", "1");
        PaillierPublicKey pk = new PaillierPublicKey(new BigInteger(paillierN),
                new BigInteger(paillierG));
        for (int i = 0; i < respondentsSize; ++i) {
            String[] cu = usersPhoneAndWeight.get(i);
            respondentsId.add(cu[0]);
            BigInteger w = pk.encrypt(new BigInteger(cu[1]));
            respondentsWeight.add(w.toString());
        }
        final JSONObject sendC = new JSONObject();
        try {
            sendC.put("settings", new JSONArray(settings));
            sendC.put("policies", new JSONArray(policies));
            sendC.put("requester", user.phone);
            sendC.put("paillier_g", paillierG);
            sendC.put("paillier_n", paillierN);
            sendC.put("ope_key", opeK);
            sendC.put("respondents", new JSONArray(respondentsId));
            sendC.put("weights", new JSONArray(respondentsWeight));
        } catch (JSONException e) {}
        Log.v("spa", sendC.toString());

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                SPAConfig.sendSPARequest,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.compareTo("ok") == 0) {
                            CharSequence text = "Send request successfully";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else {
                            CharSequence text = "You have send request before";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
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
                params.put("content", sendC.toString());
                return params;
            }
        };
        queue.add(stringRequest);
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didSelectContact(TLRPC.User user, String param) {
        if (user == null) {
            return;
        }
        usersId.add(new SPAUser(user.id, UNPROFESSIONAL_USER));
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
            if (i == usersId.size()) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public int getCount() {
            if (usersId.isEmpty()) {
                return 0;
            }
            return usersId.size() + 2;
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
                    view = new UserCell(mContext, 1);
                }
                TLRPC.User user = MessagesController.getInstance().getUser(usersId.get(i).userId);
                if (user != null) {
                    usersPhoneAndWeight.add(new String[]{user.phone, "1"});
                    ((UserCell) view).setData(user, null, user.phone != null && user.phone.length() != 0 ? PhoneFormat.getInstance().format("+" + user.phone) : LocaleController.getString("NumberUnknown", R.string.NumberUnknown), 0);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    ((TextInfoCell) view).setText(LocaleController.getString("CancelUserText", R.string.CancelUserText));
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                    TextSettingsCell textCell = (TextSettingsCell) view;
                    textCell.setText(LocaleController.getString("SPASendRequest", R.string.SPAReceivedRequest), true);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            int usersSize = usersId.size();
            if(i < usersSize) {
                return 0;
            } else if (i == usersSize) {
                return 1;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return usersId.isEmpty();
        }
    }
}
