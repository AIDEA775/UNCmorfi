package com.uncmorfi.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.uncmorfi.R;


public class HelpFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.help_list);

        String[] questions = getResources().getStringArray(R.array.help_string_array_questions);
        String[] answers = getResources().getStringArray(R.array.help_string_array_answers);

        ExpandableListAdapter listAdapter = new HelpExpandableListAdapter(questions, answers);

        listView.setAdapter(listAdapter);

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_help);
    }
}