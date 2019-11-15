package com.uncmorfi.balance

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.R
import com.uncmorfi.shared.onTextChanged
import kotlinx.android.synthetic.main.view_user_new.view.*

class NewUserView: LinearLayout {
    private lateinit var doneListener: (String) -> Unit
    private lateinit var scannerListener: Fragment

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_user_new, this, true)

        newUserInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        newUserInput.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            false
        }

        newUserInput.onTextChanged { onTextChanged(it) }

        // Por defecto llama al lector
        newUserScanner.setOnClickListener { onScanner() }
    }

    private fun onTextChanged(s: CharSequence) {
        newUserScanner.apply {
            if (s.isNotEmpty()) {
                // Cuando hay texto, cambia la función del botón por newUser.
                setImageResource(R.drawable.ic_done)
                contentDescription = context.getString(R.string.balance_new_user_button_enter)
                setOnClickListener {
                    onDone()
                }
            } else {
                // Si es está vacio, llama al lector.
                setImageResource(R.drawable.ic_barcode)
                contentDescription = context.getString(R.string.balance_new_user_button_code)
                setOnClickListener {

                }
            }
        }
    }

    private fun onDone() {
        val input = newUserInput.text.toString()
        doneListener(input)
    }

    // Inicia el lector de barras.
    // Devuelve el resultado por scannerListener.onActivityResult.
    private fun onScanner() {
        val integrator = IntentIntegrator.forSupportFragment(scannerListener)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
        integrator.setPrompt(context.getString(R.string.balance_align_barcode))
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    fun init(f: Fragment, l: (String) -> Unit) {
        scannerListener = f
        doneListener = l
    }

    fun clearText() {
        newUserInput.text.clear()
    }

}