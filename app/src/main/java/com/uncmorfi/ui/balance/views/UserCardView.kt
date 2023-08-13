package com.uncmorfi.ui.balance.views

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.databinding.ViewUserCardBinding
import com.uncmorfi.shared.WARNING_USER_RATIONS
import com.uncmorfi.shared.colorOf
import com.uncmorfi.shared.visible
import java.time.Instant
import java.time.LocalDate
import java.util.*

class UserCardView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attr, defStyleAttr) {

    private lateinit var user: User
    private val binding = ViewUserCardBinding.inflate(LayoutInflater.from(context),this)

    fun setUser(u: User) {
        user = u
        setText(user)
        setImage(user)
        setColors(user)
        setProgressBar(user.isLoading)
    }

    private fun setText(user: User) {
        binding.userName.text = user.name
        binding.userCard.text = user.card

        binding.userBalance.text = user.balanceOrRations()
        binding.userLastUpdate.text = context.getString(
            R.string.balance_last_update, relativeLastUpdate(user.lastUpdate)
        )

        val rations = user.calculateRations()?.let {
            context.resources.getQuantityString(R.plurals.balance_ration, it, it)
        }
        binding.userRation.text = rations
        binding.userRation.visible(rations != null)
    }

    private fun relativeLastUpdate(lastUpdate: Instant): String {
        return DateUtils
            .getRelativeTimeSpanString(lastUpdate.toEpochMilli())
            .toString()
            .lowercase(Locale.getDefault())
    }

    private fun setImage(user: User) {
        Glide.with(context)
            .load(user.image)
            .placeholder(R.drawable.ic_account)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.userImage)
    }

    private fun setColors(user: User) {
        // Alerta si le queda poco saldo
        binding.userBalance.setTextColor(
            context.colorOf(
                if (user.anyRations() <= WARNING_USER_RATIONS) R.color.accent
                else R.color.primary_dark
            )
        )
        binding.userRation.setTextColor(
            context.colorOf(
                if (user.anyRations() <= WARNING_USER_RATIONS) R.color.accent
                else R.color.primary_text
            )
        )

        // Alerta si la tarjeta está vencida o está por vencerse
        when {
            warning(user.expiration, 0) -> {
                setBackgroundColor(context.colorOf(R.color.accent))
                binding.userBalance.setTextColor(context.colorOf(R.color.white))
                setTextColor(R.color.white, R.color.white)
            }
            warning(user.expiration, WARNING_USER_EXPIRE) -> {
                setBackgroundColor(context.colorOf(R.color.white))
                setTextColor(R.color.primary_text, R.color.secondary_text)
            }
            else -> {
                setBackgroundColor(context.colorOf(R.color.white))
                setTextColor(R.color.primary_text, R.color.secondary_text)
            }
        }
    }

    private fun setTextColor(primary: Int, extra: Int) {
        binding.userName.setTextColor(context.colorOf(primary))

        binding.userCard.setTextColor(context.colorOf(extra))
        binding.userLastUpdate.setTextColor(context.colorOf(extra))
    }

    private fun warning(expiration: LocalDate, months: Long): Boolean {
        return expiration.isBefore(LocalDate.now().plusMonths(months))
    }

    private fun setProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.userBar.visibility = View.VISIBLE
            scaleView(binding.userImage, SCALE_USER_IMAGE_SIZE, SCALE_USER_IMAGE_SIZE)
        } else {
            binding.userBar.visibility = View.INVISIBLE
            scaleView(binding.userImage, 1f, 1f)
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
        private const val WARNING_USER_EXPIRE = 1L
    }
}