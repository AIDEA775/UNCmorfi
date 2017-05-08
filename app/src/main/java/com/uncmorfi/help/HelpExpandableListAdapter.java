package com.uncmorfi.help;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.uncmorfi.R;


class HelpExpandableListAdapter extends BaseExpandableListAdapter {
    private String[] mHeaders;
    private String[] mSubHeaders;

    HelpExpandableListAdapter(String[] headers, String[] items) {
        mHeaders = headers;
        mSubHeaders = items;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return mSubHeaders[listPosition];
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String subHeadText = (String) getChild(listPosition, expandedListPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_answer, parent, false);
        }

        TextView subHeadTextView = (TextView) convertView.findViewById(R.id.help_answer);
        subHeadTextView.setText(subHeadText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int listPosition) {
        return mHeaders[listPosition];
    }

    @Override
    public int getGroupCount() {
        return mHeaders.length;
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String headTitle = (String) getGroup(listPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);
        }

        TextView headTitleTextView = (TextView) convertView.findViewById(R.id.help_question);
        headTitleTextView.setText(headTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return false;
    }
}
