package com.example.applicationlock.lock.widget;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.applicationlock.MyApplication;
import com.example.applicationlock.R;
import com.example.applicationlock.util.LogUtil;

import java.util.ArrayList;

public class MenuDialogFragment extends DialogFragment {
    private static final String TAG = "CustomMenuDialogFragment";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MENU_ITEM = "menu_item";

    private MenuItemAdapter menuItemAdapter;

    public interface ItemClick {
        void itemClick(int position, String item);
    }

    private ItemClick itemClick;
    private TextView mTvTitle;
    private ListView mLvMenuItem;

    public MenuDialogFragment() {

    }

    public static MenuDialogFragment newInstance(String title, ArrayList<String> menuItems) {
        MenuDialogFragment fragment = new MenuDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putStringArrayList(KEY_MENU_ITEM, menuItems);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setMenuItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_menu_item_dialog, container,
                false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String title = getArguments().getString(KEY_TITLE, getResources()
                .getString(R.string.text_default_title));
        ArrayList<String> items = getArguments().getStringArrayList(KEY_MENU_ITEM);
        mTvTitle = (TextView) view.findViewById(R.id.menu_item_dialog_title);
        mLvMenuItem = (ListView) view.findViewById(R.id.lv_menu);
        mTvTitle.setText(title);
        menuItemAdapter = new MenuItemAdapter(MyApplication.getInstance().getContext(), items);
        mLvMenuItem.setAdapter(menuItemAdapter);

        mLvMenuItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (itemClick != null) {
                    itemClick.itemClick(position, menuItemAdapter.getItem(position));
                    dismiss();
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private class MenuItemAdapter extends BaseAdapter {
        private static final String TAG = "MenuItemAdapter";
        private Context mContext;
        private ArrayList<String> items;

        public MenuItemAdapter(Context context, ArrayList<String> items) {
            this.mContext = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public String getItem(int position) {
            if (items == null) {
                return null;
            }

            if (position < 0 || position > items.size() - 1) {
                return null;
            }

            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_item, null);
                viewHolder = new ViewHolder();
                viewHolder.item = convertView.findViewById(R.id.tv_menu_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.item.setText(getItem(position));

            return convertView;
        }

        class ViewHolder {
            TextView item;
        }
    }
}
