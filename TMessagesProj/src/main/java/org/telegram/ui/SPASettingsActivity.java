package org.telegram.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Created by zqguo on 2015/10/26.
 */
public class SPASettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListAdapter listAdapter;

    private int spaRequstRow;
    private int selectPrivacyItemsRow;
    private int friendsListRow;
    private int sendSpaRequstRow;
    private int spaRequstDetailRow;
    private int spaResultRow;
    private int spaResultRow2;
    private int spaResultDetailRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        // need ContactsController.getInstance().loadPrivacySettings();

        rowCount = 0;
        spaRequstRow = rowCount++;
        selectPrivacyItemsRow = rowCount++;
        friendsListRow = rowCount++;
        sendSpaRequstRow = rowCount++;
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
               if (i == selectPrivacyItemsRow)  {
                   Log.v("SPA", "select privacy items");
                   presentFragment(new SelectPrivacyItems());
               } else if (i == friendsListRow) {
                   Log.v("SPA", "friends list");
               } else if (i == sendSpaRequstRow) {
                   Log.v("SPA", "send spa request");
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
            return i == spaRequstRow || i == selectPrivacyItemsRow || i == friendsListRow || i == spaRequstDetailRow || i == sendSpaRequstRow || i == spaResultRow || i == spaResultRow2 || i == spaResultDetailRow;
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
                } else if (i == sendSpaRequstRow) {
                    textCell.setText(LocaleController.getString("SPASendRequest", R.string.SPASendRequest), true);
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
            if (position == selectPrivacyItemsRow || position == friendsListRow || position == sendSpaRequstRow || position == spaResultRow2) {
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
