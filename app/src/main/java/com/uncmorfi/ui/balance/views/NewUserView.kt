package com.uncmorfi.ui.balance.views

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.uncmorfi.R
import com.uncmorfi.databinding.ViewUserNewBinding
import com.uncmorfi.shared.invisible
import com.uncmorfi.shared.onTextChanged

class NewUserView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attr, defStyleAttr) {

    private lateinit var doneListener: (String) -> Unit

    private val binding = ViewUserNewBinding.inflate(LayoutInflater.from(context),this)

    init {
        binding.setUi()
    }

    private fun ViewUserNewBinding.setUi(){
        newUserInput.apply {
            filters = arrayOf<InputFilter>(InputFilter.AllCaps())
            setOnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    onDone()
                }
                false
            }
            onTextChanged {
                newUserDone.invisible(it.isBlank())
            }
        }
        newUserDone.apply {
            invisible(true)
            contentDescription = context.getString(R.string.balance_new_user_button_enter)
            setOnClickListener { onDone() }
        }
    }

    private fun onDone() {
        val input = binding.newUserInput.text.toString().trim()
        doneListener(input)
    }

    fun onDone(l: (String) -> Unit) {
        doneListener = l
    }

    fun clearText() {
        binding.newUserInput.text.clear()
    }

}