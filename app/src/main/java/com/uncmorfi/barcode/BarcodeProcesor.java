package com.uncmorfi.barcode;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


class BarcodeProcesor implements Detector.Processor<Barcode> {
    private CallbackFound listener;

    interface CallbackFound {
        void onFound(String barcodeValue);
    }

    BarcodeProcesor(CallbackFound listener){
        this.listener = listener;
    }

    public void receiveDetections (Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

        if (barcodes.size() != 0) {
            listener.onFound(barcodes.valueAt(0).rawValue);
        }
    }

    public void release () {

    }
}