package com.uncmorfi.balance

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uncmorfi.R
import com.uncmorfi.balance.model.User
import kotlinx.android.synthetic.main.user_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.helpers.colorOf

/**
 * Llena un RecyclerView.
 */
internal class UserCursorAdapter(private val mContext: Context,
                                 private val mClickListener: (User) -> Unit,
                                 private val mLongClickListener: (User) -> Unit) :
                                 RecyclerView.Adapter<UserCursorAdapter.UserItemViewHolder>() {
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private var mCursor: Cursor? = null
    private val mUpdateInProgress = ArrayList<Boolean>()

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(user: User) {
            setText(user)
            setImage(user)
            setColors(user)
            setProgressBar(user.position!!)
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
                    .placeholder(R.drawable.person_placeholder)
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

        private fun setProgressBar(position: Int) {
            if (mUpdateInProgress[position]) {
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
        mCursor?.let {
            it.moveToPosition(position)
            val user = User(it)
            user.position = position
            holder.bind(user)
        }
    }

    override fun getItemCount(): Int {
        return mCursor?.count ?: 0
    }

    fun setCursor(newCursor: Cursor?) {
        mCursor = newCursor
        resizeUpdateInProgress()
    }

    private fun resizeUpdateInProgress() {
        val cursor = mCursor
        if (cursor != null) {
            val diff = cursor.count - mUpdateInProgress.size
            if (diff > 0) {
                val list = Arrays.asList<Boolean>(*arrayOfNulls(diff))
                list.fill(false)
                mUpdateInProgress.addAll(list)
            }
        }
    }

    fun setInProgress(position: Int, show: Boolean) {
        mUpdateInProgress[position] = show
    }

    companion object {
        private const val SCALE_USER_IMAGE_SIZE = 0.8f
        private const val SCALE_USER_IMAGE_TIME = 500L
        private const val WARNING_USER_BALANCE = 20
        private const val WARNING_USER_EXPIRE = 1
    }
}
