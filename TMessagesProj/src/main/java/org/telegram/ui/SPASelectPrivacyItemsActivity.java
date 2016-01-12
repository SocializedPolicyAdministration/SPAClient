package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SPAConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by zqguo on 2015/10/27.
 */
public class SPASelectPrivacyItemsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private ListAdapter listAdapter;

    private int privacySectionRow;
    private int lastSeenRow;
    private int privacyDetailRow;
    private int securitySectionRow;
    private int passcodeLock;
    private int securityDetailRow;
    private int rowCount;
    private int testSectionRow;
    private int average;
    private int minMax;
    private int testDetailedRow;

    private final int LAST_SEEN = 0;
    private final int PASSCODE_LOCK = 1;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        privacySectionRow = rowCount++;
        lastSeenRow = rowCount++;
        privacyDetailRow = rowCount++;
        securitySectionRow = rowCount++;
        passcodeLock = rowCount++;
        securityDetailRow = rowCount++;
        testSectionRow = rowCount++;
        average = rowCount++;
        minMax = rowCount++;
        testDetailedRow = rowCount++;

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
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SPASelectItems", R.string.SPASelectItems));
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
        //frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (i == lastSeenRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean last_seen_setting = preferences.getBoolean("last_seen_setting", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("last_seen_setting", !last_seen_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!last_seen_setting);
                    }
                } else if (i == passcodeLock) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("passcode_lock_setting", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("passcode_lock_setting", !passcode_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!passcode_setting);
                    }
                } else if (i == average) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("average_policy", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("average_policy", !passcode_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!passcode_setting);
                    }
                } else if (i == minMax) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("maximum_minimum_policy", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("maximum_minimum_policy", !passcode_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!passcode_setting);
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == lastSeenRow || i == passcodeLock) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                    CharSequence[] items = new CharSequence[]{
                            LocaleController.getString("MajorityPreferred", R.string.MajorityPreferred),
                            LocaleController.getString("MinorityPreferred", R.string.MinorityPreferred),
                    };
                    final int selectItem = i;
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            String selectOne;
                            if (selectItem == passcodeLock) {
                                selectOne = "passcode_lock_setting_policy";
                            } else if (selectItem == lastSeenRow) {
                                selectOne = "last_seen_setting_policy";
                            } else {
                                return;
                            }
                            if (j == 0) {
                                editor.putString(selectOne, "MajorityPreferred");
                            } else if (j == 1) {
                                editor.putString(selectOne, "MinorityPreferred");
                            }
                            editor.commit();
                        }
                    });
                    showDialog(builder.create());
                } else if (i == minMax) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                    CharSequence[] items = new CharSequence[]{
                            LocaleController.getString("MaximumValue", R.string.MaximumValue),
                            LocaleController.getString("MinimumValue", R.string.MinimumValue)
                    };
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(SPAConfig.SPA_PREFERENCE, Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            String selectOne = "maximum_minimum_policy_policy";
                            if (j == 0) {
                                editor.putString(selectOne, "MaximumValue");
                            } else if (j == 1) {
                                editor.putString(selectOne, "MinimumValue");
                            }
                            editor.commit();
                        }
                    });
                    showDialog(builder.create());
                } else {
                    // do nothing
                }

                return true;
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
            return i == privacyDetailRow || i == privacySectionRow || i == lastSeenRow
                    || i == securityDetailRow || i == securitySectionRow || i == passcodeLock
                    || i == testSectionRow || i == average || i == minMax || i == testDetailedRow;
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
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("spaconfig", Activity.MODE_PRIVATE);
                if (i == lastSeenRow) {
                    textCell.setTextAndCheck(LocaleController.getString("PrivacyLastSeen", R.string.PrivacyLastSeen), preferences.getBoolean("last_seen_setting", false), false);
                } else if (i == passcodeLock) {
                    textCell.setTextAndCheck(LocaleController.getString("Passcode", R.string.Passcode), preferences.getBoolean("passcode_lock_setting", false), false);
                } else if (i == average) {
                    textCell.setTextAndCheck("Average Policy", preferences.getBoolean("average_policy", false), false);
                } else if (i == minMax) {
                    textCell.setTextAndCheck("Maximum/Minimum Policy", preferences.getBoolean("maximum_minimum_policy", false), false);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == privacyDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SPAPrivacyDetailHelp", R.string.SPAPrivacyDetailHelp));
                    view.setBackgroundResource(R.drawable.greydivider);
                } else if (i == securityDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SPASecurityDetailHelp", R.string.SPASecurityDetailHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                } else if (i == testDetailedRow) {
                    ((TextInfoPrivacyCell) view).setText("Test Average policy and Maximum/Minimum policy. Long tap for policy type");
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == privacySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("PrivacyTitle", R.string.PrivacyTitle));
                } else if (i == securitySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SecurityTitle", R.string.SecurityTitle));
                } else if (i == testSectionRow) {
                    ((HeaderCell) view).setText("Test");
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == lastSeenRow || position == passcodeLock
                    || position == average || position == minMax) {
                return 0;
            } else if (position == privacyDetailRow || position == securityDetailRow
                    || position == testDetailedRow) {
                return 1;
            } else if (position == privacySectionRow || position == securitySectionRow
                    || position == testSectionRow)  {
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
