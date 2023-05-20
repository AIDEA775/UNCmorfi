package com.uncmorfi.balance

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.uncmorfi.R
import com.uncmorfi.shared.invisible
import com.uncmorfi.shared.onTextChanged
import kotlinx.android.synthetic.main.view_user_new.view.*

class NewUserView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {

    private lateinit var doneListener: (String) -> Unit

    init {
        LayoutInflater.from(context).inflate(R.layout.view_user_new, this, true)

        newUserInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        newUserInput.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            false
        }

        newUserInput.onTextChanged {
            newUserDone.invisible(it.isBlank())
        }

        newUserDone.invisible(true)
        newUserDone.contentDescription = context.getString(R.string.balance_new_user_button_enter)
        newUserDone.setOnClickListener { onDone() }
    }

    private fun onDone() {
        val input = newUserInput.text.toString().trim()
        doneListener(input)
    }

    fun onDone(l: (String) -> Unit) {
        doneListener = l
    }

    fun clearText() {
        newUserInput.text.clear()
    }

}