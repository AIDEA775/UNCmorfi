package com.uncmorfi.balance

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.uncmorfi.R
import com.uncmorfi.balance.backend.BalanceBackend
import com.uncmorfi.balance.model.User
import com.uncmorfi.helpers.MemoryHelper
import java.util.*

class BarcodeActivity : AppCompatActivity() {
    private var mFrame: ImageView? = null
    private var mBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barcode)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        val intent = this.intent
        val user = intent.getSerializableExtra(USER_ARG) as User

        val userName = findViewById<TextView>(R.id.user_name)
        val userCard = findViewById<TextView>(R.id.user_card)
        val userType = findViewById<TextView>(R.id.user_type)
        mFrame = findViewById(R.id.barcode_frame)

        userName.text = user.name
        userCard.text = user.card
        userType.text = user.type

        mBitmap = getBarcodeBitmap(user.card)
        mFrame?.setImageBitmap(mBitmap)
    }

    private fun getBarcodeBitmap(card: String?): Bitmap? {
        var b = MemoryHelper.readBitmapFromStorage(
                applicationContext, BalanceBackend.BARCODE_PATH + card)
        if (b == null) {
            b = generateBarcode(card)
            MemoryHelper.saveBitmapToStorage(
                    applicationContext, BalanceBackend.BARCODE_PATH + card, b)
        }
        return b
    }

    private fun generateBarcode(card: String?): Bitmap? {
        val writer = MultiFormatWriter()
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 0

        val bitmap: Bitmap
        try {
            val bitMatrix = writer.encode(card, BarcodeFormat.CODE_39, 2200, 550, hints)
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }

        return bitmap
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFrame != null) {
            mFrame?.setImageBitmap(null)
            mFrame = null
        }
        if (mBitmap != null) {
            mBitmap?.recycle()
            mBitmap = null
        }
    }

    companion object {
        const val USER_ARG = "user"
    }
}
