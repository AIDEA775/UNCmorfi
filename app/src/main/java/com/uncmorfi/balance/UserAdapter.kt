package com.uncmorfi.balance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.models.User


internal class UserAdapter(private val mContext: Context,
                           private val mClickListener: (User) -> Unit,
                           private val mLongClickListener: (User) -> Unit) :
                                 RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {
    private var mUsersList = emptyList<User>()

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(user: User) {
            user.showOn(itemView)
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
}
