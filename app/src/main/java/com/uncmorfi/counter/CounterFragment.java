package com.uncmorfi.counter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;

import java.util.Locale;


public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener {
    // Toolbar
    MenuItem refreshItem;

    // UI
    private TextView resume;
    private ProgressBar bar;
    private TextView percent;

    // Tarea asincrona
    private AsyncTask thread;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // TODO cambiar la animacion por un progressBar
        // TODO crear un fab para actualizar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        resume = (TextView) view.findViewById(R.id.counter_resume);
        bar = (ProgressBar) view.findViewById(R.id.counter_bar);
        percent = (TextView) view.findViewById(R.id.counter_percent);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter, menu);
        refreshItem = menu.findItem(R.id.action_sync_counter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync_counter) {
            refreshCounter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (thread != null) {
            thread.cancel(true);
        }
        animationStop();
    }

    private void refreshCounter() {
        if (ConnectionHelper.isOnline(getContext())) {
            bar.setMax(1500);
            bar.setProgress(0);
            this.thread = new RefreshCounterTask(this).execute();
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.no_connection),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshCounter(int code, int progress) {
        switch (code) {
            case ConnectionHelper.CONNECTION_ERROR:
                Toast.makeText(getContext(),
                        getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                        .show();
                break;
            case ConnectionHelper.ERROR:
                break;
            case ConnectionHelper.SUCCESS:
                animationStop();
                bar.setProgress(progress);
                resume.setText(String.format(Locale.US, "%d raciones de %d", progress, 1500));
                percent.setText(String.format(Locale.US, "%d%%", (progress*100)/1500));

                Toast.makeText(getContext(),
                        getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                        .show();
                break;
            case RefreshCounterTask.RefreshCounterListener.ANIM_START:
                animationStart();
                break;
            case RefreshCounterTask.RefreshCounterListener.ANIM_STOP:
                animationStop();
                break;
            default:
                break;
        }
    }

    private void animationStart() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.icon_refresh, null);

        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
    }

    private void animationStop() {
        if (refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }
}