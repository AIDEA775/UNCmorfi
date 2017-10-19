package com.uncmorfi.balance.dialogs;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;
import com.uncmorfi.balance.model.User;

import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_BACKEND;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_USER;

public class BarcodeDialog extends DialogFragment {
    BalanceBackend mBackend;
    String mUserCard;

    View mRootView;
    TextView mText;
    ProgressBar mBar;
    ImageView mFrame;
    Handler mHandler;
    Bitmap mBitmap;

    /**
     * @param user Puede no contener todos los datos del usuario, pero necesita:
     *             {@link User#getCard()}
     */
    public static BarcodeDialog newInstance(User user, BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putSerializable(ARG_USER, user);
        args.putSerializable(ARG_BACKEND, backend);

        BarcodeDialog fragment = new BarcodeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final User user = (User) getArguments().getSerializable(ARG_USER);

        if (user != null) mUserCard =  user.getCard();
        mBackend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_barcode, container, false);

        mText = (TextView) mRootView.findViewById(R.id.barcode_card);
        mBar = (ProgressBar) mRootView.findViewById(R.id.barcode_bar);
        mFrame = (ImageView) mRootView.findViewById(R.id.barcode_frame);

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run () {
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int w = metrics.heightPixels;
                int h = (int) (metrics.widthPixels * 0.7);

                mBitmap = mBackend.getBarcodeBitmap(mUserCard, w, h);

                mHandler.post(new Runnable() {
                    @Override
                    public void run () {
                        if (getActivity() != null && isAdded()) {
                            mBar.setVisibility(View.GONE);
                            mText.setVisibility(View.VISIBLE);
                            mText.setText(mUserCard);
                            mFrame.setImageBitmap(mBitmap);
                        }
                    }
                });
            }
        }).start();

        return mRootView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mFrame != null) {
            mFrame.setImageBitmap(null);
            mFrame = null;
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
