package com.uncmorfi.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.MemoryHelper;

public class MenuFragment extends Fragment implements RefreshMenuTask.RefreshMenuListener {
    public static final String MENU_FILE = "menu.txt";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mWebView = (WebView) view.findViewById(R.id.menu_content);
        mWebView.loadData(MemoryHelper.readFromInternalMemory(getActivity(), MENU_FILE),
                "text/html","UTF-8");

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.menu_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshMenu();
                    }
                }
        );

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.action_sync_menu) {
            refreshMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshMenu() {
        if (ConnectionHelper.isOnline(getContext())) {
            mSwipeRefreshLayout.setRefreshing(true);
            new RefreshMenuTask(getActivity(), this).execute();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mWebView, R.string.no_connection, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRefreshMenuSuccess(String menu) {
        if (isAdded()) {
            mWebView.loadDataWithBaseURL(null, menu, "text/html", "UTF-8", null);

            Snackbar.make(mWebView, R.string.refresh_success, Snackbar.LENGTH_LONG)
                    .show();

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefreshMenuFail() {
        if (isAdded()) {
            Snackbar.make(mWebView, R.string.refresh_fail, Snackbar.LENGTH_LONG)
                    .show();

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}