package com.uncmorfi.help;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.uncmorfi.R;


class QuestionViewHolder extends ParentViewHolder {
    private TextView mTextView;
    private ImageView mArrowExpand;

    QuestionViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.help_question);

        mArrowExpand = (ImageView) itemView.findViewById(R.id.help_icon);
        mArrowExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded()) {
                    collapseView();
                } else {
                    expandView();
                }
            }
        });
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);
        if (isExpanded()) {
            mArrowExpand.animate()
                    .rotation(-180)
                    .setDuration(200)
                    .start();
        } else {
            mArrowExpand.animate()
                    .rotation(0)
                    .setDuration(200)
                    .start();
        }
    }

    void bind(Question question) {
        mTextView.setText(question.getQuestion());
    }
}