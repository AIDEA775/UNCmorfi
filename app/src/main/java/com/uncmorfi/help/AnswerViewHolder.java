package com.uncmorfi.help;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.uncmorfi.R;


class AnswerViewHolder extends ChildViewHolder {
    private TextView mTextView;

    AnswerViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.help_answer);
    }

    void bind(String answer) {
        mTextView.setText(answer);
    }
}
