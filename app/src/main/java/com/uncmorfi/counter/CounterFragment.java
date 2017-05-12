package com.uncmorfi.counter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.SnackbarHelper;

import java.util.Locale;


public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener {
    private final static int FOOD_RATIONS = 1500;
    private TextView mResumeView;
    private ProgressBar mProgressBar;
    private TextView mPercentView;
    private FloatingActionButton mRefreshFab;
    private View mRootView;
    private Snackbar lastSnackBar;

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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_counter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lastSnackBar != null && lastSnackBar.isShown())
            lastSnackBar.dismiss();
    }

    private void refreshCounter() {
        if (ConnectionHelper.isOnline(getContext())) {
            hideRefreshButton();
            new RefreshCounterTask(this).execute();
        } else {
            showRefreshButton();
            showSnackBarMsg(R.string.no_connection, SnackbarHelper.SnackType.ERROR);
        }
    }

    private void showRefreshButton() {
        mProgressBar.setIndeterminate(false);
        mRefreshFab.show();
    }

    private void hideRefreshButton() {
        mProgressBar.setIndeterminate(true);
        mRefreshFab.hide();
    }

    @Override
    public void onRefreshCounterSuccess(int percent) {
        if (isAdded()) {
            showRefreshButton();
            mProgressBar.setProgress(percent);
            mResumeView.setText(String.format(getString(R.string.counter_rations_title), percent,
                    FOOD_RATIONS));
            mPercentView.setText(String.format(Locale.US, "%d%%", (percent*100) / FOOD_RATIONS));

            showSnackBarMsg(R.string.update_success, SnackbarHelper.SnackType.FINISH);
        }
    }

    @Override
    public void onRefreshCounterFail() {
        if (isAdded()) {
            showRefreshButton();
            showSnackBarMsg(R.string.connection_error, SnackbarHelper.SnackType.ERROR);
        }
    }

    private void showSnackBarMsg(int resId, SnackbarHelper.SnackType type) {
        lastSnackBar = Snackbar.make(mRootView, resId, SnackbarHelper.getLength(type));
        SnackbarHelper.getColored(getContext(), lastSnackBar, type).show();
    }
}