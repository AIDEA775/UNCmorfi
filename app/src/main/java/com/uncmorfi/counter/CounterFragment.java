package com.uncmorfi.counter;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;

import java.util.Locale;


public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener {
    private TextView mResumeView;
    private ProgressBar mProgressBar;
    private TextView mPercentView;
    private FloatingActionButton mRefreshFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        mResumeView = (TextView) view.findViewById(R.id.counter_resume);
        mProgressBar = (ProgressBar) view.findViewById(R.id.counter_bar);
        mPercentView = (TextView) view.findViewById(R.id.counter_percent);

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
            mProgressBar.setIndeterminate(true);
            hideRefreshButton();
            new RefreshCounterTask(this).execute();
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.no_connection),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showRefreshButton() {
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
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mResumeView.setText(String.format(Locale.US, "%d raciones de %d", percent, 1500));
            mPercentView.setText(String.format(Locale.US, "%d%%", (percent*100)/1500));

            showRefreshButton();

            Toast.makeText(getContext(),
                    getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRefreshCounterFail() {
        if (isAdded()) {
            showRefreshButton();

            Toast.makeText(getContext(),
                    getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}