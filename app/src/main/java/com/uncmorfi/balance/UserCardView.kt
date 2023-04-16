package com.uncmorfi.balance

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.colorOf
import com.uncmorfi.shared.toFormat
import kotlinx.android.synthetic.main.view_user_card.view.*
import java.util.*

class UserCardView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attr, defStyleAttr) {

    private lateinit var mUser: User

    init {
        LayoutInflater.from(context).inflate(R.layout.view_user_card, this, true)
    }

    fun setUser(u: User) {
        mUser = u
        setText(mUser)
        setImage(mUser)
        setColors(mUser)
        setProgressBar(mUser.isLoading)
    }

    private fun setText(user: User) {
        userName.text = user.name
        userCard.text = user.card
        userType.text = user.type

        userBalance.text = String.format(Locale.US, "$ %d", user.balance)
        userExpiration.text = context.getString(R.string.balance_expiration)
            .format(textExpiration(user.expiration))
        userLastUpdate.text = context.getString(R.string.balance_last_update)
            .format(relativeLastUpdate(user.lastUpdate))
    }

    private fun textExpiration(expiration: Calendar?): String {
        return expiration?.toFormat("yyyy-MM-dd") ?: "?"
    }

    private fun relativeLastUpdate(lastUpdate: Calendar): String {
        return DateUtils
            .getRelativeTimeSpanString(lastUpdate.timeInMillis)
            .toString()
            .lowercase(Locale.getDefault())
    }

    private fun setImage(user: User) {
        Glide.with(context)
            .load(user.image)
            .placeholder(R.drawable.ic_account)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.circleCropTransform())
            .into(userImage)
    }

    private fun setColors(user: User) {
        // Alerta si le queda poco saldo
        if (user.balance < WARNING_USER_BALANCE)
            userBalance.setTextColor(context.colorOf(R.color.accent))
        else
            userBalance.setTextColor(context.colorOf(R.color.primary_dark))

        // Alerta si la tarjeta está vencida o está por vencerse
        when {
            warning(user.expiration, 0) -> {
                setBackgroundColor(context.colorOf(R.color.accent))
                userBalance.setTextColor(context.colorOf(R.color.white))
                userExpiration.setTypeface(null, Typeface.BOLD)
                userExpiration.setTextColor(context.colorOf(R.color.white))
                setTextColor(R.color.white, R.color.white)
            }
            warning(user.expiration, WARNING_USER_EXPIRE) -> {
                setBackgroundColor(context.colorOf(R.color.white))
                userExpiration.setTypeface(null, Typeface.NORMAL)
                userExpiration.setTextColor(context.colorOf(R.color.accent))
                setTextColor(R.color.primary_text, R.color.secondary_text)
            }
            else -> {
                setBackgroundColor(context.colorOf(R.color.white))
                userExpiration.setTypeface(null, Typeface.NORMAL)
                userExpiration.setTextColor(context.colorOf(R.color.secondary_text))
                setTextColor(R.color.primary_text, R.color.secondary_text)
            }
        }
    }

    private fun setTextColor(primary: Int, extra: Int) {
        userName.setTextColor(context.colorOf(primary))
        userCard.setTextColor(context.colorOf(primary))

        userType.setTextColor(context.colorOf(extra))
        userLastUpdate.setTextColor(context.colorOf(extra))
    }

    private fun warning(expiration: Calendar, months: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MONTH, months)
        return expiration.before(cal)
    }

    private fun setProgressBar(isLoading: Boolean) {
        if (isLoading) {
            userBar.visibility = View.VISIBLE
            scaleView(userImage, SCALE_USER_IMAGE_SIZE, SCALE_USER_IMAGE_SIZE)
        } else {
            userBar.visibility = View.INVISIBLE
            scaleView(userImage, 1f, 1f)
        }
    }

    private fun scaleView(v: View, endX: Float, endY: Float) {
        val anim = ScaleAnimation(
            v.scaleX, endX,
            v.scaleY, endY,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.fillAfter = true
        anim.duration = SCALE_USER_IMAGE_TIME
        v.startAnimation(anim)
    }

    companion object {
        private const val SCALE_USER_IMAGE_SIZE = 0.8f
        private const val SCALE_USER_IMAGE_TIME = 500L
        private const val WARNING_USER_BALANCE = 20
        private const val WARNING_USER_EXPIRE = 1
    }
}