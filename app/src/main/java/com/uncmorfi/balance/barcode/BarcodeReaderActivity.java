package com.uncmorfi.balance.barcode;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.uncmorfi.R;

import java.io.IOException;


public class BarcodeReaderActivity extends AppCompatActivity implements
        BarcodeProcesor.CallbackFound {
    public static final String REQUEST_DATA = "barcode";

    CameraSource mCameraSource;
    BarcodeDetector mBarcodeDetector;
    SurfaceView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);

        mCameraView = (SurfaceView)findViewById(R.id.camera_view);

        mBarcodeDetector = new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.CODE_39)
                        .build();

        mCameraSource = new CameraSource
                .Builder(this, mBarcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(30.0f)
                .setAutoFocusEnabled(true)
                .build();

        mBarcodeDetector.setProcessor(new BarcodeProcesor(this));

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCameraSource();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });
    }

    @Override
    public void onFound (String barcodeValue) {
        Intent data = new Intent();
        data.putExtra(REQUEST_DATA, barcodeValue);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void startCameraSource() {
        try {
            mCameraSource.start(mCameraView.getHolder());
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

}




