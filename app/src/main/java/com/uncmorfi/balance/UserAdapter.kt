package com.uncmorfi.balance

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.R
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.models.User
import kotlinx.android.synthetic.main.user_item.view.*
import java.text.SimpleDateFormat
import java.util.*


internal class UserAdapter(private val mContext: Context,
                           private val mClickListener: (User) -> Unit,
                           private val mLongClickListener: (User) -> Unit) :
                                 RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private var mUsersList = emptyList<User>()

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(user: User) {
            setText(user)
            setImage(user)
            setColors(user)
            setProgressBar(user.isLoading)
            itemView.setOnClickListener { mClickListener(user) }
            itemView.setOnLongClickListener { mLongClickListener(user); true }
        }

        private fun setText(user: User) {
            itemView.userName.text = user.name
            itemView.userCard.text = user.card
            itemView.userType.text = user.type

            itemView.userBalance.text = String.format(Locale.US, "$ %d", user.balance)
            itemView.userExpiration.text = mContext.getString(R.string.balance_expiration)
                    .format(textExpiration(user.expiration))
            itemView.userLastUpdate.text = mContext.getString(R.string.balance_last_update)
                    .format(relativeLastUpdate(user.lastUpdate))
        }

        private fun textExpiration(expiration: Long): String {
            return if (expiration == 0L)
                "?"
            else
                mDateFormat.format(Date(expiration))
        }

        private fun relativeLastUpdate(lastUpdate: Long): String {
            return DateUtils.getRelativeTimeSpanString(lastUpdate).toString().toLowerCase()
        }

        private fun setImage(user: User) {
            Glide.with(mContext)
                    .load(user.image)
                    .placeholder(R.drawable.ic_account)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(itemView.userImage)
        }

        private fun setColors(user: User) {
            // Alerta si le queda poco saldo
            if (user.balance < WARNING_USER_BALANCE)
                itemView.userBalance.setTextColor(mContext.colorOf(R.color.accent))
            else
                itemView.userBalance.setTextColor(mContext.colorOf(R.color.primary_dark))

            // Alerta si la tarjeta está vencida o está por vencerse
            when {
                expired(user.expiration) -> {
                    itemView.setBackgroundColor(mContext.colorOf(R.color.accent))
                    itemView.userBalance.setTextColor(mContext.colorOf(R.color.white))
                    itemView.userExpiration.setTypeface(null, Typeface.BOLD)
                    itemView.userExpiration.setTextColor(mContext.colorOf(R.color.white))
                    setTextColor(R.color.white, R.color.white)
                }
                warningExpiration(user.expiration) -> {
                    itemView.setBackgroundColor(mContext.colorOf(R.color.white))
                    itemView.userExpiration.setTypeface(null, Typeface.NORMAL)
                    itemView.userExpiration.setTextColor(mContext.colorOf(R.color.accent))
                    setTextColor(R.color.primary_text, R.color.secondary_text)
                }
                else -> {
                    itemView.setBackgroundColor(mContext.colorOf(R.color.white))
                    itemView.userExpiration.setTypeface(null, Typeface.NORMAL)
                    itemView.userExpiration.setTextColor(mContext.colorOf(R.color.secondary_text))
                    setTextColor(R.color.primary_text, R.color.secondary_text)
                }
            }
        }

        private fun setTextColor(primary: Int, extra: Int) {
            itemView.userName.setTextColor(mContext.colorOf(primary))
            itemView.userCard.setTextColor(mContext.colorOf(primary))

            itemView.userType.setTextColor(mContext.colorOf(extra))
            itemView.userLastUpdate.setTextColor(mContext.colorOf(extra))
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

        private fun setProgressBar(isLoading: Boolean) {
            if (isLoading) {
                itemView.userBar.visibility = View.VISIBLE
                scaleView(itemView.userImage, SCALE_USER_IMAGE_SIZE, SCALE_USER_IMAGE_SIZE)
            } else {
                itemView.userBar.visibility = View.INVISIBLE
                scaleView(itemView.userImage,1f,1f)
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

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        return UserItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val user = mUsersList.getOrNull(position)
        user?.let {
            holder.bind(user)
        }
    }

    override fun getItemCount(): Int {
        return mUsersList.count()
    }

    fun setUsers(users: List<User>) {
        mUsersList = users
        notifyDataSetChanged()
    }

    companion object {
        private const val SCALE_USER_IMAGE_SIZE = 0.8f
        private const val SCALE_USER_IMAGE_TIME = 500L
        private const val WARNING_USER_BALANCE = 20
        private const val WARNING_USER_EXPIRE = 1
    }
}
