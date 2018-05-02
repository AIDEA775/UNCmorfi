package com.uncmorfi.balance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.helpers.MemoryHelper;

import java.util.EnumMap;
import java.util.Map;

import static com.uncmorfi.balance.backend.BalanceBackend.BARCODE_PATH;

public class BarcodeActivity extends AppCompatActivity {
    public static final String USER_ARG = "user";
    private ImageView mFrame;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barcode);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Intent intent = this.getIntent();
        User user = (User) intent.getSerializableExtra(USER_ARG);

        TextView userName = findViewById(R.id.user_name);
        TextView userCard = findViewById(R.id.user_card);
        mFrame = findViewById(R.id.barcode_frame);

        userName.setText(user.getName());
        userCard.setText(user.getCard());

        mBitmap = getBarcodeBitmap(user.getCard());
        mFrame.setImageBitmap(mBitmap);
    }


    public Bitmap getBarcodeBitmap(String card) {
        Bitmap b = MemoryHelper.readBitmapFromStorage(getApplicationContext(), BARCODE_PATH + card);
        if (b == null) {
            b = generateBarcode(card);
            MemoryHelper.saveBitmapToStorage(getApplicationContext(), BARCODE_PATH + card, b);
        }
        return b;
    }

    private Bitmap generateBarcode(String card) {
        MultiFormatWriter writer = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);

        try {
            BitMatrix bitMatrix = writer.encode(card, BarcodeFormat.CODE_39, 2200, 550, hints);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
