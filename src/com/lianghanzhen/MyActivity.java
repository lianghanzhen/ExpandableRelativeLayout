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
            for (int i = 0; i < position + 1; i++) {
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
                viewHolder.mContainer = (ExpandableTextView) convertView.findViewById(R.id.container);
                convertView.setTag(R.id.tag, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.id.tag);
                viewHolder.mContainer.setTag(R.id.tag_expandable_text_view_reused, new Object());
                viewHolder.mContainer.setExpanded(mExpanded.get(position));
            }
            viewHolder.mContainer.setTag(position);
            viewHolder.mContainer.setText(getItem(position));
            viewHolder.mContainer.setOnExpandListener(new ExpandableTextView.OnExpandListener() {
                @Override
                public void onExpand(ExpandableTextView parent) {
                    mExpanded.put(position, true);
                }
            }).setOnCollapseListener(new ExpandableTextView.OnCollapseListener() {
                @Override
                public void onCollapse(ExpandableTextView parent) {
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
            ExpandableTextView mContainer;
        }

    }

}
