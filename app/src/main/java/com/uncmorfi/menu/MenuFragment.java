package com.uncmorfi.menu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.MemoryHelper;
import com.uncmorfi.helpers.RecyclerTouchListener;
import com.uncmorfi.helpers.SnackbarHelper.SnackType;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.uncmorfi.helpers.SnackbarHelper.showSnack;

/**
 * Menú de la semana.
 * Administra la UI y el guardado persistente del menú.
 * Usa a {@link RefreshMenuTask} para actualizar el mra la UI y el guardado persistente del menú.
 * Usa a {@link RefreshMenuTask} para actualizar el menú.
 */
public class MenuFragment extends Fragment implements RefreshMenuTask.RefreshMenuListener {
    private static final String URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana";
    public static final String MENU_FILE = "menu";
    private View mRootView;
    private MenuAdapter mMenuAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Context mApplicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_menu, container, false);

        mSwipeRefreshLayout = mRootView.findViewById(R.id.menu_swipe_refresh);
        mApplicationContext = getActivity().getApplicationContext();

        initSwipeRefreshLayout();
        initRecyclerAndAdapter();
        initMenu();

        if (needAutoRefreshMenu())
            refreshMenu();

        return mRootView;
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

    private void initRecyclerAndAdapter() {
        mRecyclerView = mRootView.findViewById(R.id.menu_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mApplicationContext, DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mApplicationContext,
                mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) { }

            @Override
            public void onLongClick(View view, int position) {
                List food = mMenuAdapter.getMenuList().get(position).getFood();
                String result = TextUtils.join(", ", food) + "\n\n#UNCmorfi";
                ClipboardManager clipboard = (ClipboardManager)
                        mApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Food", result);
                clipboard.setPrimaryClip(clip);
                showSnack(getContext(), mRootView, R.string.menu_copy_msg, SnackType.FINISH);
            }
        }));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void initMenu() {
        String menuSaved = MemoryHelper.readStringFromStorage(getContext(), MENU_FILE);
        List<DayMenu> menuList;

        if (menuSaved != null)
            menuList = DayMenu.fromJson(menuSaved);
        else
            menuList = new ArrayList<>();

        mMenuAdapter = new MenuAdapter(mApplicationContext, menuList);
        mRecyclerView.setAdapter(mMenuAdapter);
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
        } else if (item.getItemId() == R.id.menu_browser) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshMenu() {
        if (ConnectionHelper.isOnline(mApplicationContext)) {
            mSwipeRefreshLayout.setRefreshing(true);
            new RefreshMenuTask(mApplicationContext,this).execute();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            showSnack(getContext(), mRootView, R.string.no_connection, SnackType.ERROR);
        }
    }

    @Override
    public void onRefreshMenuSuccess(List<DayMenu> menuList) {
        if (getActivity() != null && isAdded()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mMenuAdapter.updateMenu(menuList);
            showSnack(getContext(), mRootView, R.string.update_success, SnackType.FINISH);
        }
    }

    @Override
    public void onRefreshMenuFail() {
        if (getActivity() != null && isAdded())
            mSwipeRefreshLayout.setRefreshing(false);
            showSnack(getContext(), mRootView, R.string.update_fail, SnackType.ERROR);
    }

}