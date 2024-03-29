package com.uncmorfi.ui.balance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.databinding.ItemUserBinding

internal class UserAdapter(
    private val mClickListener: (User) -> Unit,
    private val mLongClickListener: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {
    private var mUsersList = emptyList<User>()

    internal inner class UserItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val binding = ItemUserBinding.bind(v)

        fun bind(user: User) {
            binding.userCardView.setUser(user)
            binding.userCardView.setOnClickListener { mClickListener(user) }
            binding.userCardView.setOnLongClickListener { mLongClickListener(user); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        mUsersList.getOrNull(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return mUsersList.count()
    }

    fun setUsers(users: List<User>) {
        mUsersList = users
        notifyDataSetChanged()
    }
}