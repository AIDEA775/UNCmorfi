package com.uncmorfi.menu;

import android.content.Context;
import android.os.Bundle;
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
import com.uncmorfi.helpers.SnackbarHelper.SnackType;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import static com.uncmorfi.helpers.SnackbarHelper.showSnack;

/**
 * Menú de la semana.
 * Administra la UI y el guardado persistente del menú.
 * Usa a {@link RefreshMenuTask} para actualizar el mra la UI y el guardado persistente del menú.
 * Usa a {@link RefreshMenuTask} para actualizar el menú.
 */
public class MenuFragment extends Fragment implements RefreshMenuTask.RefreshMenuListener {
    public static final String MENU_FILE = "menu.txt";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WebView mWebView;
    private Context mApplicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mWebView = view.findViewById(R.id.menu_content);
        mSwipeRefreshLayout = view.findViewById(R.id.menu_swipe_refresh);
        mApplicationContext = getActivity().getApplicationContext();

        initSwipeRefreshLayout();
        initMenu();

        if (needAutoRefreshMenu())
            refreshMenu();

        return view;
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshMenu();
                    }
                }
        );

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(
                R.color.accent);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.white,
                R.color.primary_light
        );
    }

    private void initMenu() {
        String menuSaved = MemoryHelper.readStringFromStorage(getContext(), MENU_FILE);
        if (menuSaved != null)
            mWebView.loadDataWithBaseURL(null, menuSaved, "text/html", "UTF-8", null);
    }

    private boolean needAutoRefreshMenu() {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        int nowWeek = now.get(Calendar.WEEK_OF_YEAR);
        int nowYear = now.get(Calendar.YEAR);

        Calendar menu = Calendar.getInstance();
        menu.setTimeInMillis(getMenuLastModified());
        int menuWeek = menu.get(Calendar.WEEK_OF_YEAR);
        int menuYear = now.get(Calendar.YEAR);

        return (menuYear < nowYear || menuWeek < nowWeek);
    }

    private long getMenuLastModified() {
        File menuFile = new File(getContext().getFilesDir() + "/" + MENU_FILE);
        return menuFile.lastModified();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_menu);
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
        if (item.getItemId() ==  R.id.menu_update) {
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
            showSnack(getContext(), mWebView, R.string.no_connection, SnackType.ERROR);
        }
    }

    @Override
    public void onRefreshMenuSuccess(String menu) {
        if (needSaveMenu(menu))
            MemoryHelper.saveToStorage(mApplicationContext, MenuFragment.MENU_FILE, menu);

        if (getActivity() != null && isAdded()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mWebView.loadDataWithBaseURL(null, menu, "text/html", "UTF-8", null);
            showSnack(getContext(), mWebView, R.string.update_success, SnackType.FINISH);
        }
    }

    @Override
    public void onRefreshMenuFail() {
        if (getActivity() != null && isAdded())
            showSnack(getContext(), mWebView, R.string.update_fail, SnackType.ERROR);
    }

    private boolean needSaveMenu(String menu) {
        String menuSaved = MemoryHelper.readHeadFromStorage(getContext(), MENU_FILE);
        return (menuSaved == null || !menu.startsWith(menuSaved));
    }

}