package com.uncmorfi.balance

import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.R
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.models.User
import kotlinx.android.synthetic.main.user_item.view.*
import java.text.SimpleDateFormat
import java.util.*

fun User.showOn(view: View) {
    setText(view, this)
    setImage(view, this)
    setColors(view, this)
    setProgressBar(view, this.isLoading)
}

private fun setText(view: View, user: User) {
    view.userName.text = user.name
    view.userCard.text = user.card
    view.userType.text = user.type

    view.userBalance.text = String.format(Locale.US, "$ %d", user.balance)
    view.userExpiration.text = view.context.getString(R.string.balance_expiration)
            .format(textExpiration(user.expiration))
    view.userLastUpdate.text = view.context.getString(R.string.balance_last_update)
            .format(relativeLastUpdate(user.lastUpdate))
}

private fun textExpiration(expiration: Long): String {
    return if (expiration == 0L) "?"
        else SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(expiration))
}

private fun relativeLastUpdate(lastUpdate: Long): String {
    return DateUtils.getRelativeTimeSpanString(lastUpdate).toString().toLowerCase()
}

private fun setImage(view: View, user: User) {
    Glide.with(view.context)
            .load(user.image)
            .placeholder(R.drawable.ic_account)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.circleCropTransform())
            .into(view.userImage)
}

private fun setColors(view: View, user: User) {
    // Alerta si le queda poco saldo
    if (user.balance < WARNING_USER_BALANCE)
        view.userBalance.setTextColor(view.context.colorOf(R.color.accent))
    else
        view.userBalance.setTextColor(view.context.colorOf(R.color.primary_dark))

    // Alerta si la tarjeta está vencida o está por vencerse
    when {
        expired(user.expiration) -> {
            view.setBackgroundColor(view.context.colorOf(R.color.accent))
            view.userBalance.setTextColor(view.context.colorOf(R.color.white))
            view.userExpiration.setTypeface(null, Typeface.BOLD)
            view.userExpiration.setTextColor(view.context.colorOf(R.color.white))
            setTextColor(view, R.color.white, R.color.white)
        }
        warningExpiration(user.expiration) -> {
            view.setBackgroundColor(view.context.colorOf(R.color.white))
            view.userExpiration.setTypeface(null, Typeface.NORMAL)
            view.userExpiration.setTextColor(view.context.colorOf(R.color.accent))
            setTextColor(view, R.color.primary_text, R.color.secondary_text)
        }
        else -> {
            view.setBackgroundColor(view.context.colorOf(R.color.white))
            view.userExpiration.setTypeface(null, Typeface.NORMAL)
            view.userExpiration.setTextColor(view.context.colorOf(R.color.secondary_text))
            setTextColor(view, R.color.primary_text, R.color.secondary_text)
        }
    }
}

private fun setTextColor(view: View, primary: Int, extra: Int) {
    view.userName.setTextColor(view.context.colorOf(primary))
    view.userCard.setTextColor(view.context.colorOf(primary))

    view.userType.setTextColor(view.context.colorOf(extra))
    view.userLastUpdate.setTextColor(view.context.colorOf(extra))
}

private fun warningExpiration(expiration: Long): Boolean {
    val cal = Calendar.getInstance()
    cal.time = Date()
    cal.add(Calendar.MONTH, WARNING_USER_EXPIRE)
    return Date(expiration).before(cal.time)
}

private fun expired(expiration: Long): Boolean {
    return Date(expiration).before(Date())
}

private fun setProgressBar(view: View, isLoading: Boolean) {
    if (isLoading) {
        view.userBar.visibility = View.VISIBLE
        scaleView(view.userImage, SCALE_USER_IMAGE_SIZE, SCALE_USER_IMAGE_SIZE)
    } else {
        view.userBar.visibility = View.INVISIBLE
        scaleView(view.userImage,1f,1f)
    }
}

private fun scaleView(v: View, endX: Float, endY: Float) {
    val anim = ScaleAnimation(
            v.scaleX, endX,
            v.scaleY, endY,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
    anim.fillAfter = true
    anim.duration = SCALE_USER_IMAGE_TIME
    v.startAnimation(anim)
}

private const val SCALE_USER_IMAGE_SIZE = 0.8f
private const val SCALE_USER_IMAGE_TIME = 500L
private const val WARNING_USER_BALANCE = 20
private const val WARNING_USER_EXPIRE = 1
