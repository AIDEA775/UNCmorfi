package com.uncmorfi.help;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.Collections;
import java.util.List;

class Question implements ParentListItem {
    private String mQuestion;
    private String mAnswer;

    Question(String question, String answers) {
        mQuestion = question;
        mAnswer = answers;
    }

    String getQuestion() {
        return mQuestion;
    }

    @Override
    public List<String> getChildItemList() {
        return Collections.singletonList(mAnswer);
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
