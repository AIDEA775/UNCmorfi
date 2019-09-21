package com.uncmorfi.balance

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.uncmorfi.R
import com.uncmorfi.models.User
import com.uncmorfi.shared.readBitmapFromStorage
import com.uncmorfi.shared.saveToStorage
import kotlinx.android.synthetic.main.activity_barcode.*
import java.util.*

class BarcodeActivity : AppCompatActivity() {
    private var mBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val user = intent.getSerializableExtra(USER_ARG) as User

        userName.text = user.name
        userCard.text = user.card
        userType.text = user.type

        mBitmap = getBarcodeBitmap(user.card)
        barcodeFrame.setImageBitmap(mBitmap)
    }

    private fun getBarcodeBitmap(card: String?): Bitmap? {
        var b = applicationContext.readBitmapFromStorage(BARCODE_PATH + card)
        if (b == null) {
            b = generateBarcode(card)
            applicationContext.saveToStorage(BARCODE_PATH + card, b)
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

    override fun onDestroy() {
        super.onDestroy()
        if (barcodeFrame != null) {
            barcodeFrame.setImageBitmap(null)
        }
        if (mBitmap != null) {
            mBitmap?.recycle()
            mBitmap = null
        }
    }

    companion object {
        const val BARCODE_PATH = "barcode-land-"
        const val USER_ARG = "user"

        fun intent(context: Context, user: User): Intent {
            val intent = Intent(context, BarcodeActivity::class.java)
            intent.putExtra(USER_ARG, user)
            return intent
        }
    }
}
