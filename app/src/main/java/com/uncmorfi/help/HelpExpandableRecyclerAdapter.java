package com.uncmorfi.help;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.uncmorfi.R;

import java.util.List;


class HelpExpandableRecyclerAdapter extends
        ExpandableRecyclerAdapter<QuestionViewHolder, AnswerViewHolder> {
    private LayoutInflater mInflater;

    HelpExpandableRecyclerAdapter(Context context, List<Question> questionList) {
        super(questionList);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public QuestionViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View recipeView = mInflater.inflate(R.layout.item_question, parentViewGroup, false);
        return new QuestionViewHolder(recipeView);
    }

    @Override
    public AnswerViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View ingredientView = mInflater.inflate(R.layout.item_answer, childViewGroup, false);
        return new AnswerViewHolder(ingredientView);
    }

    @Override
    public void onBindParentViewHolder(QuestionViewHolder parentViewHolder, int position,
                                       ParentListItem parentListItem) {
        parentViewHolder.bind((Question) parentListItem);

    }

    @Override
    public void onBindChildViewHolder(AnswerViewHolder childViewHolder, int position,
                                      Object childListItem) {
        childViewHolder.bind((String) childListItem);
    }
}
