package com.uncmorfi.balance.dialogs;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.uncmorfi.R;


import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_CARD;

public class BarcodeDialog extends DialogFragment {
    String mUserCard;
    View mRootView;
    TextView mText;
    ProgressBar mBar;
    ImageView mFrame;
    Handler mHandler;

    public static BarcodeDialog newInstance(String userCard) {
        Bundle args = new Bundle();

        args.putString(ARG_CARD, userCard);

        BarcodeDialog fragment = new BarcodeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserCard =  getArguments().getString(ARG_CARD);
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
                final Bitmap result = generateBarcode();
                mHandler.post(new Runnable() {
                    @Override
                    public void run () {
                        mBar.setVisibility(View.GONE);
                        mText.setVisibility(View.VISIBLE);
                        mText.setText(mUserCard);
                        mFrame.setImageBitmap(result);
                    }
                });
            }
        }).start();

        return mRootView;
    }

    Bitmap generateBarcode() {
        MultiFormatWriter writer = new MultiFormatWriter();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int h = (int) (metrics.widthPixels * 0.7);
        int w = metrics.heightPixels;

        try {
            BitMatrix bitMatrix = writer.encode(mUserCard, BarcodeFormat.CODE_39, w, h);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
