package com.uncmorfi.counter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import com.uncmorfi.helpers.SnackbarHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CounterFragment extends Fragment implements RefreshCounterTask.RefreshCounterListener,
        SeekBar.OnSeekBarChangeListener {
    private final static int FOOD_RATIONS = 1500;
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.ROOT);

    private TextView mResumeView;
    private ProgressBar mProgressBar;
    private TextView mPercentView;
    private FloatingActionButton mRefreshFab;
    private View mRootView;
    private Snackbar lastSnackBar;
    private LineChart mChart;
    private SeekBar mSeekBar;
    private TextView mEstimateView;
    private TextView mDistanceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        mResumeView = (TextView) view.findViewById(R.id.counter_resume);
        mProgressBar = (ProgressBar) view.findViewById(R.id.counter_bar);
        mProgressBar.setMax(FOOD_RATIONS);
        mPercentView = (TextView) view.findViewById(R.id.counter_percent);
        mRootView = view.findViewById(R.id.counter_coordinator);
        mEstimateView = (TextView) view.findViewById(R.id.counter_estimate);
        mDistanceView = (TextView) view.findViewById(R.id.counter_distance);
        mSeekBar = (SeekBar) view.findViewById(R.id.counter_seek);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(0);

        mRefreshFab = (FloatingActionButton) view.findViewById(R.id.counter_fab);
        mRefreshFab.setScaleX(0);
        mRefreshFab.setScaleY(0);
        mRefreshFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshCounter();
            }
        });

        mChart = (LineChart) view.findViewById(R.id.counter_chart);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress += 1;

        long estimateMin = getEstimateFromPosition(progress);
        long currentTime = new Date().getTime();

        Date estimateDate = new Date(currentTime + estimateMin * 60 * 1000);
        String time = mDateFormat.format(estimateDate);

        mEstimateView.setText(String.format(getString(R.string.counter_estimate),
                estimateMin, time));
        mDistanceView.setText(String.format(getString(R.string.counter_distance), progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    // Devolver el tiempo estimado en minutos dependiendo de la distancia x
    private long getEstimateFromPosition(int x) {
        return (long) (x * (x * 0.062307 + 1.296347) - 2.190814);
    }

    @Override
    public void onRefreshCounterSuccess(JSONArray result) {
        int total = 0;
        List<Entry> data = new ArrayList<>();

        for (int i = 0; i < result.length(); i++) {
            try {
                JSONObject item = result.getJSONObject(i);

                Date date = parseStringToDate(item.getString("fecha"));
                int ration = Integer.parseInt(item.getString("raciones"));

                if (date != null) data.add(new Entry(date.getTime(), ration));
                total += ration;
            } catch (JSONException|NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (isAdded()) {
            showRefreshButton();

            updateText(total);
            updateChart(data);

            showSnackBarMsg(R.string.update_success, SnackbarHelper.SnackType.FINISH);
        }
    }

    private Date parseStringToDate(String string) {
        try {
            return mDateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateText(int total) {
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

    private class HourAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date valueDate = (new Date((long) value));
            return mDateFormat.format(valueDate);
        }
    }
}