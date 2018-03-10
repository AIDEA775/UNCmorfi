package com.uncmorfi.counter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.uncmorfi.R;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.SnackbarHelper.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.uncmorfi.helpers.SnackbarHelper.showSnack;

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 * Usa a {@link RefreshCounterTask} para actualizar el medidor.
 */
public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener,
        SeekBar.OnSeekBarChangeListener {
    private final static int FOOD_RATIONS = 1500;
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.ROOT);

    private View mRootView;
    private TextView mResumeView;
    private ProgressBar mProgressBar;
    private TextView mPercentView;
    private TextView mDistanceView;
    private SeekBar mSeekBar;
    private TextView mEstimateView;
    private LineChart mChart;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        setAllViews(view);
        setChartOptions();
        initSwipeRefreshLayout();

        mProgressBar.setMax(FOOD_RATIONS);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(0);

        refreshCounter();
        return view;
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshCounter();
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

    private void setAllViews(View view) {
        mRootView = view.findViewById(R.id.counter_coordinator);
        mResumeView = view.findViewById(R.id.counter_resume);
        mProgressBar = view.findViewById(R.id.counter_bar);
        mPercentView = view.findViewById(R.id.counter_percent);
        mDistanceView = view.findViewById(R.id.counter_distance);
        mSeekBar = view.findViewById(R.id.counter_seek);
        mEstimateView = view.findViewById(R.id.counter_estimate);
        mChart = view.findViewById(R.id.counter_chart);
        mSwipeRefreshLayout = view.findViewById(R.id.counter_swipe_refresh);
    }

    private void setChartOptions() {
        mChart.getLegend().setEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setScaleYEnabled(false);
        mChart.setDescription(null);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBorders(false);
        mChart.setHighlightPerTapEnabled(false);
        mChart.setHighlightPerDragEnabled(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        IAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter();
        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
    }

    private void refreshCounter() {
        if (ConnectionHelper.isOnline(getContext())) {
            showRefreshStatus();
            new RefreshCounterTask(this).execute();
        } else {
            hideRefreshStatus();
            showSnack(getContext(), mRootView, R.string.no_connection, SnackType.ERROR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_counter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.counter_update) {
            refreshCounter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = progress + 5; // Mínimo 5 ventanas

        long minutes = getEstimateFromPosition(progress);
        long currentTime = new Date().getTime();

        Date timeStamp = new Date(currentTime + minutes * 60 * 1000);
        String text = mDateFormat.format(timeStamp);

        mDistanceView.setText(String.format(getString(R.string.counter_distance), progress));
        mEstimateView.setText(String.format(getString(R.string.counter_estimate), minutes, text));
    }

    /**
     * Estimar el tiempo de espera.
     * @param x Distancia (cantidad de ventanas)
     * @return Minutos estimados
     */
    private long getEstimateFromPosition(int x) {
        return (long) (x * (x * 0.062307 + 1.296347) - 2.190814);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onRefreshCounterSuccess(List<Entry> result) {
        int total = 0;
        if (isAdded()) {
            for (Entry entry : result)
                total += entry.getY();

            hideRefreshStatus();
            updateTextViews(total);
            updateChart(result);

            showSnack(getContext(), mRootView, R.string.update_success, SnackType.FINISH);
        }
    }

    @Override
    public void onRefreshCounterFail(int errorCode) {
        if (isAdded()) {
            hideRefreshStatus();
            showError(errorCode);
        }
    }

    private void updateTextViews(int total) {
        mProgressBar.setProgress(total);
        mResumeView.setText(String.format(getString(R.string.counter_rations_title), total,
                FOOD_RATIONS));
        mPercentView.setText(String.format(Locale.US, "%d%%", (total*100) / FOOD_RATIONS));
    }

    private void updateChart(List<Entry> data) {
        if (data != null && !data.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(data, null);
            setLineDataSetStyle(dataSet);

            LineData lineData = new LineData(dataSet);
            mChart.setData(lineData);
            mChart.setVisibleXRangeMinimum(1000000f);
            mChart.invalidate();
        }
    }

    private void setLineDataSetStyle(LineDataSet dataSet) {
        dataSet.setColors(new int[] {R.color.accent}, getContext());
        dataSet.setCircleColors(new int[] {R.color.accent}, getContext());
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5);
        dataSet.setDrawValues(false);
    }

    private void showRefreshStatus() {
        mProgressBar.setIndeterminate(true);
        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void hideRefreshStatus() {
        mProgressBar.setIndeterminate(false);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void showError(int code) {
        switch (code) {
            case ConnectionHelper.CONNECTION_ERROR:
                showSnack(getContext(), mRootView, R.string.connection_error, SnackType.ERROR);
                break;
            case ConnectionHelper.INTERNAL_ERROR:
                showSnack(getContext(), mRootView, R.string.internal_error, SnackType.ERROR);
        }
    }

    private class HourAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date valueDate = (new Date((long) value));
            return mDateFormat.format(valueDate);
        }
    }
}