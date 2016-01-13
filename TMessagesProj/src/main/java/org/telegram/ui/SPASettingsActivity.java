package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SPAConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.volley.Request;
import org.telegram.messenger.volley.RequestQueue;
import org.telegram.messenger.volley.Response;
import org.telegram.messenger.volley.VolleyError;
import org.telegram.messenger.volley.toolbox.StringRequest;
import org.telegram.messenger.volley.toolbox.Volley;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqguo on 2015/10/26.
 */
public class SPASettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;

    private int spaRequstRow;
    private int selectPrivacyItemsRow;
    private int friendsListRow;
    private int receivedSpaRequstRow;
    private int spaRequstDetailRow;
    private int spaResultRow;
    private int spaResultRow2;
    private int spaResultDetailRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        // SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
        // SharedPreferences.Editor editor = preferences.edit();
        // editor.clear();
        // editor.commit();

        // need ContactsController.getInstance().loadPrivacySettings();

        rowCount = 0;
        spaRequstRow = rowCount++;
        selectPrivacyItemsRow = rowCount++;
        friendsListRow = rowCount++;
        receivedSpaRequstRow = rowCount++;
        spaRequstDetailRow = rowCount++;
        spaResultRow = rowCount++;
        spaResultRow2 = rowCount++;
        spaResultDetailRow = rowCount++;


        NotificationCenter.getInstance().addObserver(this, NotificationCenter.spaSettings);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.spaSettings);
    }

    @Override
    public View createView(Context context) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
        if (!preferences.contains("paillier_lambda")) {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = SPAConfig.addKeys;
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                            if (response.charAt(0) != '0' && response.charAt(0) != '1') {
                                Log.v("SPA", response);
                            } else {
                                String keys[] = response.split(" ");
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("paillier_lambda", keys[0]);
                                editor.putString("paillier_mu", keys[1]);
                                editor.putString("paillier_g", keys[2]);
                                editor.putString("paillier_n", keys[3]);
                                editor.putString("ope_key", keys[4]);
                                editor.commit();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("SPA", "SPA Setting cannot connect keymanager!");
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", UserConfig.getCurrentUser().phone);
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SPASettings", R.string.SPASettings));
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
                if (i == selectPrivacyItemsRow) {
                    Log.v("SPA", "select privacy items");
                    presentFragment(new SPASelectPrivacyItemsActivity());
                } else if (i == friendsListRow) {
                    Log.v("SPA", "friends list");
                    presentFragment(new SPAFriendListActivity());
                } else if (i == receivedSpaRequstRow) {
                    Log.v("SPA", "received spa request");
                    presentFragment(new SPAReceivedSPARequest());
                } else if (i == spaResultRow2) {
                    Log.v("SPA", "spa result");
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.spaSettings) {
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
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
            return i == spaRequstRow || i == selectPrivacyItemsRow || i == friendsListRow || i == spaRequstDetailRow || i == receivedSpaRequstRow || i == spaResultRow || i == spaResultRow2 || i == spaResultDetailRow;
        }

        @Override
        public int getCount() {
            return rowCount;
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
                if (i == selectPrivacyItemsRow) {
                    textCell.setText(LocaleController.getString("SPASelectItems", R.string.SPASelectItems), true);
                } else if (i == friendsListRow) {
                    textCell.setText(LocaleController.getString("SPAFriendsList", R.string.SPAFriendsList), true);
                } else if (i == receivedSpaRequstRow) {
                    textCell.setText(LocaleController.getString("SPAReceivedRequest", R.string.SPAReceivedRequest), true);
                } else if (i == spaResultRow2) {
                    textCell.setText(LocaleController.getString("SPAResult", R.string.SPAResult), true);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == spaRequstDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SPAResultHelp", R.string.SPAResultHelp));
                    view.setBackgroundResource(R.drawable.greydivider);
                } else if (i == spaResultDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SPASendingHelp", R.string.SPASendingHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == spaRequstRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SPARequestTitle", R.string.SPARequestTitle));
                } else if (i == spaResultRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SPAResultTitle", R.string.SPAResultTitle));
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == selectPrivacyItemsRow || position == friendsListRow || position == receivedSpaRequstRow || position == spaResultRow2) {
                return 0;
            } else if (position == spaRequstDetailRow || position == spaResultDetailRow) {
                return 1;
            } else if (position == spaRequstRow || position == spaResultRow)  {
                return 2;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
