package com.uncmorfi.menu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uncmorfi.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder> {
    private DateFormat mDateNumber = new SimpleDateFormat("dd", Locale.getDefault());
    private DateFormat mDateName = new SimpleDateFormat("EEE", Locale.getDefault());
    private List<DayMenu> mMenuList;
    private Context mContext;

    static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumberText;
        TextView dayNameText;
        TextView food1Text;
        TextView food2Text;
        TextView food3Text;

        MenuItemViewHolder(View v) {
            super(v);

            dayNumberText = v.findViewById(R.id.menu_day_number);
            dayNameText = v.findViewById(R.id.menu_day_name);
            food1Text = v.findViewById(R.id.menu_food1);
            food2Text = v.findViewById(R.id.menu_food2);
            food3Text = v.findViewById(R.id.menu_food3);
        }
    }


    MenuAdapter(Context context, List<DayMenu> menu) {
        mContext = context;
        mMenuList = menu;
    }

    @Override
    public @NonNull MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item, parent, false);
        return new MenuItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        DayMenu day = mMenuList.get(position);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int offset = fmt.format(day.getDate()).compareTo(fmt.format(new Date()));

        int colorDay;
        int colorFood;
        int colorBack;
        if (offset < 0) {
            colorDay = ContextCompat.getColor(mContext, R.color.secondary_text);
            colorFood = ContextCompat.getColor(mContext, R.color.secondary_text);
            colorBack = ContextCompat.getColor(mContext, R.color.white);
        } else if (offset == 0) {
            colorDay = ContextCompat.getColor(mContext, R.color.white);
            colorFood = ContextCompat.getColor(mContext, R.color.white);
            colorBack = ContextCompat.getColor(mContext, R.color.accent);

        } else {
            colorDay = ContextCompat.getColor(mContext, R.color.primary_text);
            colorFood = ContextCompat.getColor(mContext, R.color.primary_text);
            colorBack = ContextCompat.getColor(mContext, R.color.white);
        }

        holder.itemView.setBackgroundColor(colorBack);
        holder.dayNumberText.setTextColor(colorDay);
        holder.dayNameText.setTextColor(colorDay);

        holder.food1Text.setTextColor(colorFood);
        holder.food2Text.setTextColor(colorFood);
        holder.food3Text.setTextColor(colorFood);

        holder.dayNumberText.setText(mDateNumber.format(day.getDate()));
        holder.dayNameText.setText(mDateName.format(day.getDate()));

        holder.food1Text.setText(day.getFood().get(0));
        holder.food2Text.setText(day.getFood().get(1));
        holder.food3Text.setText(day.getFood().get(2));
    }

    @Override
    public int getItemCount() {
        if (mMenuList != null)
            return mMenuList.size();
        return 0;
    }

    public void updateMenu(List<DayMenu> menuList) {
        mMenuList = menuList;
        notifyDataSetChanged();
    }

    public List<DayMenu> getMenuList() {
        return mMenuList;
    }
}