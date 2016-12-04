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
import org.telegram.ui.Cells.TextCheckCell;
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
    private int spaCloseAverageComposite;
    private int spaCloseAverageComposite2;
    private int spaCloseAverageCompositeDetailRow;
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
        spaCloseAverageComposite = rowCount++;
        spaCloseAverageComposite2 = rowCount++;
        spaCloseAverageCompositeDetailRow = rowCount++;



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
                            char firstChar = response.charAt(0);
                            if (firstChar < '0' || firstChar > '9') {
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
                    presentFragment(new SPAResultActivity());
                } else if (i == spaCloseAverageComposite2) {
                    Log.v("SPA", "spa disable average policy");
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("disable_average_policy", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disable_average_policy", !passcode_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!passcode_setting);
                    }
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
            return i == spaRequstRow || i == selectPrivacyItemsRow || i == friendsListRow
                    || i == spaRequstDetailRow || i == receivedSpaRequstRow || i == spaResultRow
                    || i == spaResultRow2 || i == spaResultDetailRow || i == spaCloseAverageComposite
                    || i == spaCloseAverageCompositeDetailRow || i == spaCloseAverageComposite2;
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
                if (i == selectPrivacyItemsRow) {
                    ((TextSettingsCell) view).setText(LocaleController.getString("SPASelectItems", R.string.SPASelectItems), true);
                } else if (i == friendsListRow) {
                    ((TextSettingsCell) view).setText(LocaleController.getString("SPAFriendsList", R.string.SPAFriendsList), true);
                } else if (i == receivedSpaRequstRow) {
                    ((TextSettingsCell) view).setText(LocaleController.getString("SPAReceivedRequest", R.string.SPAReceivedRequest), true);
                } else if (i == spaResultRow2) {
                    ((TextSettingsCell) view).setText(LocaleController.getString("SPAResult", R.string.SPAResult), true);
                } else if (i == spaCloseAverageComposite2) {
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                    TextCheckCell textCell = (TextCheckCell) view;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("spaconfig", Activity.MODE_PRIVATE);
                    textCell.setTextAndCheck("Disable Average Policy", preferences.getBoolean("disable_average_policy", false), false);
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
                } else if (i == spaCloseAverageCompositeDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SPADisableCompositeHelp", R.string.SPACloseHelp));
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
                } else if (i == spaCloseAverageComposite) {
                    ((HeaderCell) view).setText(LocaleController.getString("SPACloseTitle", R.string.SPACloseTitle));
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == selectPrivacyItemsRow || position == friendsListRow
                    || position == receivedSpaRequstRow || position == spaResultRow2
                    || position == spaCloseAverageComposite2) {
                return 0;
            } else if (position == spaRequstDetailRow || position == spaResultDetailRow || position == spaCloseAverageCompositeDetailRow) {
                return 1;
            } else if (position == spaRequstRow || position == spaResultRow || position == spaCloseAverageComposite)  {
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
