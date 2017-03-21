package com.uncmorfi.balance.barcode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.uncmorfi.R;

import java.io.IOException;


public class BarcodeReaderActivity extends AppCompatActivity implements
        BarcodeProcessor.CallbackFound {
    public static final String ARG_BARCODE_CARD = "barcode";
    private static final int REQUEST_PERMISSION_CAMERA = 1;

    private CameraSource mCameraSource;
    private SurfaceView mCameraView;

    private boolean mSurfaceAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);

        mCameraView = (SurfaceView) findViewById(R.id.camera_view);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionCamera();
        } else {
            createCameraSource();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    private void requestPermissionCamera() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CAMERA);
    }

    private void createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_39)
                .build();

        mCameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(30.0f)
                .setAutoFocusEnabled(true)
                .build();

        barcodeDetector.setProcessor(new BarcodeProcessor(this));

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceAvailable = true;
                startCameraSource();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceAvailable = false;
                mCameraSource.stop();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            } else {
                showPermissionNotGrantedToast();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showPermissionNotGrantedToast() {
        Toast.makeText(this, R.string.balance_barcode_permission_not_granted, Toast.LENGTH_LONG)
                .show();
    }

    private void startCameraSource() {
        if (mCameraSource != null && mSurfaceAvailable) {
            try {
                mCameraSource.start(mCameraView.getHolder());
            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFound (String barcodeValue) {
        Intent data = new Intent();
        data.putExtra(ARG_BARCODE_CARD, barcodeValue);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

}