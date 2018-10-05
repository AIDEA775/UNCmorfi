package com.uncmorfi.balance

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.uncmorfi.R
import com.uncmorfi.balance.model.User
import com.uncmorfi.balance.model.UsersContract
import kotlinx.android.synthetic.main.user_item.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Llena un RecyclerView.
 * El fragmento o actividad contenedora debería implementar [OnCardClickListener].
 */
internal class UserCursorAdapter(private val mContext: Context,
                                 private val mClickListener: (User) -> Unit,
                                 private val mLongClickListener: (User) -> Boolean) :
        RecyclerView.Adapter<UserCursorAdapter.UserItemViewHolder>() {
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private var mCursor: Cursor? = null
    private val mUpdateInProgress = ArrayList<Boolean>()

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(user: User) {
            setText(user)
            setImage(user)
            setProgressBar(user.position)
            itemView.setOnClickListener { mClickListener(user) }
            itemView.setOnLongClickListener { mLongClickListener(user) }
        }

        private fun setText(user: User) {
            itemView.userName.text = user.name
            itemView.userCard.text = user.card
            itemView.userType.text = user.type
            itemView.userBalance.text = String.format(Locale.US, "$ %d", user.balance)

            itemView.userBalance.setTextColor(ContextCompat.getColor(mContext,
                    if (user.balance < WARNING_USER_BALANCE) R.color.accent else R.color.primary_dark))

            itemView.userExpiration.text = String.format(
                    mContext.getString(R.string.balance_expiration),
                    textExpiration(user.expiration))

            itemView.userExpiration.setTextColor(ContextCompat.getColor(mContext,
                    if (warningExpiration(user.expiration)) R.color.accent else R.color.secondary_text))

            itemView.userLastUpdate.text = String.format(
                    mContext.getString(R.string.balance_last_update),
                    DateUtils.getRelativeTimeSpanString(user.lastUpdate).toString().toLowerCase())
        }

        private fun textExpiration(expiration: Long): String {
            return if (expiration == 0L)
                "?"
            else
                mDateFormat.format(Date(expiration))
        }

        private fun warningExpiration(expiration: Long): Boolean {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.MONTH, WARNING_USER_EXPIRE)
            return Date(expiration).before(cal.time)
        }

        private fun setImage(user: User) {
            Glide.with(mContext)
                    .load(user.image)
                    .asBitmap()
                    .placeholder(R.drawable.person_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into<BitmapImageViewTarget>(object : BitmapImageViewTarget(itemView.userImage) {
                        override fun setResource(resource: Bitmap) {
                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                                    mContext.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            itemView.userImage.setImageDrawable(circularBitmapDrawable)
                        }
                    })
        }

        private fun setProgressBar(position: Int) {
            if (mUpdateInProgress[position]) {
                itemView.userBar.visibility = View.VISIBLE
                setUserImageUpdatingMode()
            } else {
                itemView.userBar.visibility = View.INVISIBLE
                setUserImageNormalMode()
            }
        }

        private fun setUserImageUpdatingMode() {
            itemView.userImage.scaleX = 1f
            itemView.userImage.scaleY = 1f
            itemView.userImage.animate()
                    .scaleX(SCALE_USER_IMAGE_SIZE)
                    .scaleY(SCALE_USER_IMAGE_SIZE)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(SCALE_USER_IMAGE_TIME.toLong())
                    .start()
        }

        private fun setUserImageNormalMode() {
            itemView.userImage.scaleX = SCALE_USER_IMAGE_SIZE
            itemView.userImage.scaleY = SCALE_USER_IMAGE_SIZE
            itemView.userImage.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(SCALE_USER_IMAGE_TIME.toLong())
                    .start()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        return UserItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val cursor = mCursor
        cursor?.let {
            cursor.moveToPosition(position)
            val user = User(cursor)
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
                list.fill(java.lang.Boolean.FALSE)
                mUpdateInProgress.addAll(list)
            }
        }
    }

    fun setInProgress(position: Int, show: Boolean) {
        mUpdateInProgress[position] = show
    }

    fun getItemCardFromCursor(position: Int): String? {
        val cursor = mCursor
        if (cursor != null) {
            cursor.moveToPosition(position)
            return cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.CARD))
        }

        return null
    }

    companion object {
        private const val SCALE_USER_IMAGE_SIZE = 0.8f
        private const val SCALE_USER_IMAGE_TIME = 500
        private const val WARNING_USER_BALANCE = 20
        private const val WARNING_USER_EXPIRE = 1
    }
}
