package org.telegram.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by zqguo on 2015/10/28.
 */
public class FriendListActivity extends BaseFragment implements ContactsActivity.ContactsActivityDelegate {

    private ListView listView;
    private ListAdapter listViewAdapter;
    private TextView emptyTextView;
    private FrameLayout progressView;
    private int selectedUserId;
    private ArrayList<Integer> usersId = new ArrayList<>();

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
    public View createView(Context context) {
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
                    fragment.setDelegate(FriendListActivity.this);
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
                if (i < usersId.size()) {
                    Bundle args = new Bundle();
                    args.putInt("user_id", usersId.get(i));
                    presentFragment(new ProfileActivity(args));
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 0 || i >= usersId.size() || getParentActivity() == null) {
                    return true;
                }
                selectedUserId = usersId.get(i);
                final int selectedItem = i;

                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                CharSequence[] items = new CharSequence[]{LocaleController.getString("Deselect", R.string.Deselect)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
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
        usersId.add(user.id);
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
            return i != usersId.size();
        }

        @Override
        public int getCount() {
            if (usersId.isEmpty()) {
                return 0;
            }
            return usersId.size() + 1;
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
                TLRPC.User user = MessagesController.getInstance().getUser(usersId.get(i));
                if (user != null) {
                    ((UserCell) view).setData(user, null, user.phone != null && user.phone.length() != 0 ? PhoneFormat.getInstance().format("+" + user.phone) : LocaleController.getString("NumberUnknown", R.string.NumberUnknown), 0);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    ((TextInfoCell) view).setText(LocaleController.getString("CancelUserText", R.string.CancelUserText));
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if(i == usersId.size()) {
                return 1;
            }
            return 0;
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
