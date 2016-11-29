package com.uncmorfi.menu;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MenuFragment extends Fragment implements RefreshMenuTask.RefreshMenuListener {
    private static final String FILE = "menu.txt";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        webView = (WebView) view.findViewById(R.id.menu_text);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadData(getMenuFromMemory(), "text/html","UTF-8");

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.menu_swiperefresh);
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
            new RefreshMenuTask(this).execute();
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.no_connection), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onNewMenuDownloaded(int code, String menu) {
        switch (code) {
            case ConnectionHelper.SUCCESS:
                webView.loadData(menu, "text/html","UTF-8");
                saveMenuToMemory(menu);

                Toast.makeText(getContext(),
                        getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                        .show();
                break;
            case ConnectionHelper.ERROR:
                // TODO: 11/29/16 Chequear si se puede cancelar la tarea
                Log.e("MenuFragment", "Cancelled RefreshMenuTask?");
                break;
            case ConnectionHelper.CONNECTION_ERROR:
                Toast.makeText(getContext(),
                        getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private String getMenuFromMemory() {
        try {
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(getContext().openFileInput(FILE)));
            String line;
            StringBuilder read = new StringBuilder();
            while((line = in.readLine()) != null) {
                read.append(line);
            }
            in.close();
            return read.toString();
        } catch (Exception ex) {
            Log.e("Files", "Error reading in memory");
            return null;
        }
    }

    private void saveMenuToMemory(String menu) {
        if (menu != null) {
            try {
                OutputStreamWriter out =
                        new OutputStreamWriter(
                                getContext().openFileOutput(FILE, Context.MODE_PRIVATE));

                out.write(menu);
                out.close();
            } catch (IOException ex) {
                Log.e("Files", "Error writing in memory");
            }
        }
    }
}