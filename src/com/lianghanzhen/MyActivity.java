package com.lianghanzhen;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MyActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setAdapter(new ExpandableAdapter(this));
    }

    private static class ExpandableAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final SparseBooleanArray mExpanded = new SparseBooleanArray();

        private ExpandableAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public String getItem(int position) {
            final StringBuilder result = new StringBuilder();
            result.append(String.format("Position: #%d", position)).append("\n");
            for (int i = 0; i < 10; i++) {
                result.append("\t").append(String.format("LINE #%d", (i + 1))).append("\n");
            }
            result.deleteCharAt(result.length() - 1);
            return result.toString();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.main, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mContainer = (ExpandableRelativeLayout) convertView.findViewById(R.id.container);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.mContainer.setExpanded(mExpanded.get(position));
            }
            viewHolder.mContainer.getExpander().setText(getItem(position));
            viewHolder.mContainer.setOnExpandListener(new ExpandableRelativeLayout.OnExpandListener() {
                @Override
                public void onExpand(ExpandableRelativeLayout parent) {
                    mExpanded.put(position, true);
                }
            }).setOnCollapseListener(new ExpandableRelativeLayout.OnCollapseListener() {
                @Override
                public void onCollapse(ExpandableRelativeLayout parent) {
                    mExpanded.put(position, false);
                }
            }).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.mContainer.toggle();
                }
            });
            return convertView;
        }

        private static class ViewHolder {
            ExpandableRelativeLayout mContainer;
        }

    }

}
