package com.uncmorfi.balance.barcode;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;


class BarcodeProcessor implements Detector.Processor<Barcode> {
    private CallbackFound mListener;

    interface CallbackFound {
        void onFound(String barcodeValue);
    }

    BarcodeProcessor(CallbackFound listener){
        mListener = listener;
    }

    @Override
    public void receiveDetections (Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

        if (barcodes.size() != 0) {
            mListener.onFound(barcodes.valueAt(0).rawValue);
        }
    }

    public void release () {

    }
}