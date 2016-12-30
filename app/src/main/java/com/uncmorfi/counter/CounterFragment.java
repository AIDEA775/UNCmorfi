package com.uncmorfi.counter;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;

import java.util.Locale;


public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener {
    private final static int FOOD_RATIONS = 1500;
    private TextView mResumeView;
    private ProgressBar mProgressBar;
    private TextView mPercentView;
    private FloatingActionButton mRefreshFab;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        mResumeView = (TextView) view.findViewById(R.id.counter_resume);
        mProgressBar = (ProgressBar) view.findViewById(R.id.counter_bar);
        mProgressBar.setMax(FOOD_RATIONS);
        mPercentView = (TextView) view.findViewById(R.id.counter_percent);
        mRootView = view.findViewById(R.id.counter_coordinator);

        mRefreshFab = (FloatingActionButton) view.findViewById(R.id.counter_fab);
        mRefreshFab.setScaleX(0);
        mRefreshFab.setScaleY(0);
        mRefreshFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshCounter();
            }
        });

        refreshCounter();

        return view;
    }

    private void refreshCounter() {
        if (ConnectionHelper.isOnline(getContext())) {
            hideRefreshButton();
            new RefreshCounterTask(this).execute();
        } else {
            showRefreshButton();
            Snackbar.make(mRootView, R.string.no_connection, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void showRefreshButton() {
        mProgressBar.setIndeterminate(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                    android.R.interpolator.fast_out_slow_in);

            mRefreshFab.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(interpolator)
                    .setDuration(600);
        }
    }

    private void hideRefreshButton() {
        mProgressBar.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                    android.R.interpolator.fast_out_slow_in);

            mRefreshFab.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setInterpolator(interpolator)
                    .setDuration(300);
        }
    }

    @Override
    public void onRefreshCounterSuccess(int percent) {
        if (isAdded()) {
            mProgressBar.setProgress(percent);
            mResumeView.setText(String.format(Locale.US, "%d raciones de %d", percent, FOOD_RATIONS));
            mPercentView.setText(String.format(Locale.US, "%d%%", (percent*100) / FOOD_RATIONS));

            showRefreshButton();

            Snackbar.make(mRootView, R.string.refresh_success, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRefreshCounterFail() {
        if (isAdded()) {
            showRefreshButton();

            Snackbar.make(mRootView, R.string.connection_error, Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}