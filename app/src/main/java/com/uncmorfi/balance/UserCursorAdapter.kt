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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.uncmorfi.R
import com.uncmorfi.balance.UserCursorAdapter.OnCardClickListener
import com.uncmorfi.balance.model.User
import com.uncmorfi.balance.model.UsersContract
import java.text.SimpleDateFormat
import java.util.*

/**
 * Llena un RecyclerView.
 * El fragmento o actividad contenedora debería implementar [OnCardClickListener].
 */
internal class UserCursorAdapter(private val mContext: Context,private val mListener: OnCardClickListener) :
        RecyclerView.Adapter<UserCursorAdapter.UserItemViewHolder>() {
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private var mCursor: Cursor? = null
    private val mUpdateInProgress = ArrayList<Boolean>()

    internal interface OnCardClickListener {
        fun onClick(userId: Int, userCard: String?, position: Int)
    }

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val nameText: TextView = v.findViewById(R.id.user_name)
        val cardText: TextView = v.findViewById(R.id.user_card)
        val typeText: TextView = v.findViewById(R.id.user_type)
        val balanceText: TextView = v.findViewById(R.id.user_balance)
        val userImage: ImageView = v.findViewById(R.id.user_image)
        val progressBar: ProgressBar = v.findViewById(R.id.user_bar)
        val expirationText: TextView = v.findViewById(R.id.user_expiration)
        val lastUpdateText: TextView = v.findViewById(R.id.user_last_update)

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val pos = this.adapterPosition
            mListener.onClick(getItemIdFromCursor(pos), getItemCardFromCursor(pos), pos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        return UserItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val cursor = mCursor
        if (cursor != null) {
            cursor.moveToPosition(position)
            val user = User(cursor)

            setHolder(holder, user)
            setImage(holder, user)
            setProgressBar(holder, position)
        }
    }

    private fun setHolder(holder: UserItemViewHolder, user: User) {
        holder.nameText.text = user.name
        holder.cardText.text = user.card
        holder.typeText.text = user.type
        holder.balanceText.text = String.format(Locale.US, "$ %d", user.balance)

        holder.balanceText.setTextColor(ContextCompat.getColor(mContext,
                if (user.balance < WARNING_USER_BALANCE) R.color.accent else R.color.primary_dark))

        holder.expirationText.text = String.format(
                mContext.getString(R.string.balance_expiration),
                textExpiration(user.expiration))

        holder.expirationText.setTextColor(ContextCompat.getColor(mContext,
                if (warningExpiration(user.expiration)) R.color.accent else R.color.secondary_text))

        holder.lastUpdateText.text = String.format(
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

    private fun setImage(holder: UserItemViewHolder, user: User) {
        Glide.with(mContext)
                .load(user.image)
                .asBitmap()
                .placeholder(R.drawable.person_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into<BitmapImageViewTarget>(object : BitmapImageViewTarget(holder.userImage) {
                    override fun setResource(resource: Bitmap) {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                                mContext.resources, resource)
                        circularBitmapDrawable.isCircular = true
                        holder.userImage.setImageDrawable(circularBitmapDrawable)
                    }
                })
    }

    private fun setProgressBar(holder: UserItemViewHolder, position: Int) {
        if (mUpdateInProgress[position]) {
            holder.progressBar.visibility = View.VISIBLE
            setUserImageUpdatingMode(holder)
        } else {
            holder.progressBar.visibility = View.INVISIBLE
            setUserImageNormalMode(holder)
        }
    }

    private fun setUserImageUpdatingMode(holder: UserItemViewHolder) {
        holder.userImage.scaleX = 1f
        holder.userImage.scaleY = 1f
        holder.userImage.animate()
                .scaleX(SCALE_USER_IMAGE_SIZE)
                .scaleY(SCALE_USER_IMAGE_SIZE)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setDuration(SCALE_USER_IMAGE_TIME.toLong())
                .start()
    }

    private fun setUserImageNormalMode(holder: UserItemViewHolder) {
        holder.userImage.scaleX = SCALE_USER_IMAGE_SIZE
        holder.userImage.scaleY = SCALE_USER_IMAGE_SIZE
        holder.userImage.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setDuration(SCALE_USER_IMAGE_TIME.toLong())
                .start()
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

    private fun getItemIdFromCursor(position: Int): Int {
        val cursor = mCursor
        if (cursor != null) {
            cursor.moveToPosition(position)
            return cursor.getInt(cursor.getColumnIndex(UsersContract.UserEntry.ID))
        }

        return 0
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
