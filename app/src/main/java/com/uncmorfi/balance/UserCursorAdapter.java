package com.uncmorfi.balance;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.uncmorfi.R;
import com.uncmorfi.balance.userModel.User;
import com.uncmorfi.balance.userModel.UsersDbHelper;

import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


class UserCursorAdapter extends RecyclerView.Adapter<UserCursorAdapter.UserViewHolder> implements
        DownloadUserAsyncTask.DownloadUserListener {
    private static final String URL_USER_IMAGES = "https://asiruws.unc.edu.ar/foto/";
    private Context context;
    private Cursor items;
    private OnCardClickListener listener;

    interface OnCardClickListener {
        void onClick(UserViewHolder holder);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameText;
        TextView cardText;
        TextView typeText;
        TextView balanceText;
        Button refreshButton;
        ImageView userImage;
        ProgressBar progressBar;

        UserViewHolder(View v) {
            super(v);

            // Referencias UI
            nameText = (TextView) v.findViewById(R.id.user_name);
            cardText = (TextView) v.findViewById(R.id.user_card);
            typeText = (TextView) v.findViewById(R.id.user_type);
            balanceText = (TextView) v.findViewById(R.id.user_balance);
            refreshButton = (Button) v.findViewById(R.id.user_refresh);
            userImage = (ImageView) v.findViewById(R.id.user_image);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(this);
        }
    }

    UserCursorAdapter(Context context, OnCardClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        items.moveToPosition(position);

        final User user = new User (items);

        holder.nameText.setText(user.getName());
        holder.cardText.setText(user.getCard());
        holder.typeText.setText(user.getType());
        holder.balanceText.setText(String.format(Locale.US, "$ %d", user.getBalance()));
        Glide.with(context)
                .load(URL_USER_IMAGES + user.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(context))
                .into(holder.userImage);

        holder.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPressRefreshButton(holder, user.getCard());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (items != null)
            return items.getCount();
        return 0;
    }

    private void onPressRefreshButton(UserViewHolder holder, String card) {
        new RefreshUserAsyncTask(this, holder).execute(card);
    }

    void swapCursor(Cursor newCursor) {
        if (newCursor != null) {
            items = newCursor;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onUserDownloaded(User user) {
        if (user != null) {
            UsersDbHelper usersDbHelper = new UsersDbHelper(context);

            usersDbHelper.updateUserBalance(user);
            swapCursor(usersDbHelper.getAllUsers());

            Toast.makeText(context,
                    context.getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(context,
                    context.getString(R.string.refresh_fail), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private class RefreshUserAsyncTask extends DownloadUserAsyncTask {
        private ProgressBar progressBar;

        RefreshUserAsyncTask(DownloadUserListener listener, UserViewHolder holder) {
            super(listener);
            progressBar = holder.progressBar;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(User user) {
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(user);
        }
    }
}
