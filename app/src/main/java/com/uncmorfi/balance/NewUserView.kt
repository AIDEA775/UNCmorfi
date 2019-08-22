package com.uncmorfi.balance

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.uncmorfi.R
import com.uncmorfi.helpers.onTextChanged
import kotlinx.android.synthetic.main.view_user_new.view.*

class NewUserView: LinearLayout {
    private lateinit var doneListener: (String) -> Unit
    private lateinit var scannerListener: () -> Unit

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
        newUserScanner.setOnClickListener { scannerListener() }
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
                setOnClickListener { scannerListener() }
            }
        }
    }

    private fun onDone() {
        val input = newUserInput.text.toString()
        doneListener(input)
    }

    fun done(l: (String) -> Unit) {
        doneListener = l
    }

    fun scanner(l: () -> Unit) {
        scannerListener = l
    }

    fun clearText() {
        newUserInput.text.clear()
    }

}