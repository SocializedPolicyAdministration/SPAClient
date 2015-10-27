package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Created by zqguo on 2015/10/27.
 */
public class SelectPrivacyItems extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private ListAdapter listAdapter;

    private int privacySectionRow;
    private int lastSeenRow;
    private int privacyDetailRow;
    private int securitySectionRow;
    private int passcodeLock;
    private int securityDetailRow;
    private int rowCount;

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
                if (i == lastSeenRow)  {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean last_seen_setting = preferences.getBoolean("last_seen_setting", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("last_seen_setting", !last_seen_setting);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!last_seen_setting);
                    }
                } else if (i == passcodeLock) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean passcode_setting = preferences.getBoolean("passcode_setting", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("passcode_setting", !passcode_setting);
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
            return i == privacyDetailRow || i == privacySectionRow || i == lastSeenRow || i == securityDetailRow || i == securitySectionRow || i == passcodeLock;
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
                }
                TextCheckCell textCell = (TextCheckCell) view;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == lastSeenRow) {
                    textCell.setTextAndCheck(LocaleController.getString("PrivacyLastSeen", R.string.PrivacyLastSeen), preferences.getBoolean("last_seen_setting", false), false);
                } else if (i == passcodeLock) {
                    textCell.setTextAndCheck(LocaleController.getString("Passcode", R.string.Passcode), preferences.getBoolean("passcode_setting", false), false);
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
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == lastSeenRow || position == passcodeLock) {
                return 0;
            } else if (position == privacyDetailRow || position == securityDetailRow) {
                return 1;
            } else if (position == privacySectionRow || position == securitySectionRow)  {
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
