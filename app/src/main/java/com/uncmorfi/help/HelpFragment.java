package com.uncmorfi.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uncmorfi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Ayuda o FAQ.
 * Usa a {@link HelpExpandableRecyclerAdapter} para llenar un {@link RecyclerView}
 */
public class HelpFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.help_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new HelpExpandableRecyclerAdapter(getContext(), getQuestionList()));
        return v;
    }

    private List<Question> getQuestionList() {
        String[] questions = getResources().getStringArray(R.array.help_string_array);

        List<Question> questionList = new ArrayList<>();
        for (int i = 0; i < questions.length; i += 2) {
            questionList.add(new Question(questions[i], questions[i+1]));
        }
        return questionList;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_help);
    }
}