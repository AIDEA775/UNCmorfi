package com.uncmorfi.balance;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UsersContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


class UserCursorAdapter extends RecyclerView.Adapter<UserCursorAdapter.UserViewHolder> {
    private static final String USER_IMAGES_URL = "https://asiruws.unc.edu.ar/foto/";
    private Context mContext;
    private Cursor mCursor;
    private List<Boolean> mUpdateInProgress = new ArrayList<>();
    private OnCardClickListener mListener;

    interface OnCardClickListener {
        void onClick(int userId, int position);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameText;
        TextView cardText;
        TextView typeText;
        TextView balanceText;
        ImageView userImage;
        ProgressBar progressBar;

        UserViewHolder(View v) {
            super(v);

            nameText = (TextView) v.findViewById(R.id.user_name);
            cardText = (TextView) v.findViewById(R.id.user_card);
            typeText = (TextView) v.findViewById(R.id.user_type);
            balanceText = (TextView) v.findViewById(R.id.user_balance);
            userImage = (ImageView) v.findViewById(R.id.user_image);
            progressBar = (ProgressBar) v.findViewById(R.id.user_bar);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getAdapterPosition();
            mListener.onClick(getItemIdFromCursor(position), position);
        }
    }

    UserCursorAdapter(Context context, OnCardClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        User user = new User(mCursor);

        setHolder(holder, user);
        setProgressBar(holder, position);
        setImage(holder, user);
    }

    private void setHolder(UserViewHolder holder, User user) {
        holder.nameText.setText(user.getName());
        holder.cardText.setText(user.getCard());
        holder.typeText.setText(user.getType());
        holder.balanceText.setText(String.format(Locale.US, "$ %d", user.getBalance()));
    }

    private void setProgressBar(UserViewHolder holder, int position) {
        if (mUpdateInProgress.get(position))
            holder.progressBar.setVisibility(View.VISIBLE);
        else
            holder.progressBar.setVisibility(View.INVISIBLE);
    }

    private void setImage(final UserViewHolder holder, User user) {
        Glide.with(mContext)
                .load(USER_IMAGES_URL + user.getImage())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.userImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.userImage.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    void setCursor(Cursor newCursor) {
        mCursor = newCursor;
        resizeUpdateInProgress();
    }

    private void resizeUpdateInProgress() {
        if (mCursor != null) {
            int diff = mCursor.getCount() - mUpdateInProgress.size();
            if (diff > 0) {
                List<Boolean> list = Arrays.asList(new Boolean[diff]);
                Collections.fill(list, Boolean.FALSE);
                mUpdateInProgress.addAll(list);
            }
        }
    }

    void setInProgress(int position, boolean show) {
        mUpdateInProgress.set(position, show);
    }

    int getItemIdFromCursor(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getInt(mCursor.getColumnIndex(UsersContract.UserEntry._ID));
    }
}
