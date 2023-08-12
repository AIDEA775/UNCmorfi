package com.uncmorfi.ui.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.uncmorfi.R
import kotlinx.android.synthetic.main.view_information_message.view.informationMessageButton
import kotlinx.android.synthetic.main.view_information_message.view.informationMessageTextView

class InformationMessageView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attr, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_information_message, this, true)
        informationMessageButton.isClickable = false
    }

    fun setMessage(message : String){
        informationMessageTextView.setText(message)
    }

    fun setButtonText(buttonText : String){
        informationMessageButton.text = buttonText
    }
}