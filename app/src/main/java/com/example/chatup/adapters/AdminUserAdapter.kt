package com.example.chatup.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.databinding.ItemAdminUserBinding
import com.example.chatup.models.User
import com.google.firebase.auth.FirebaseAuth

class AdminUserAdapter(
    private val users: List<User>,
    private val onUserStatusChanged: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUserViewHolder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminUserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class AdminUserViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserEmail.text = user.email
            binding.switchActive.isChecked = user.isActive
            binding.switchAdmin.isChecked = user.isAdmin

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            // Disable switches for self to prevent self-deactivation/demotion
            if (user.uid == currentUserId) {
                binding.switchActive.isEnabled = false
                binding.switchAdmin.isEnabled = false
            } else {
                binding.switchActive.isEnabled = true
                binding.switchAdmin.isEnabled = true
            }

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                if (user.isActive != isChecked) {
                    user.isActive = isChecked
                    onUserStatusChanged(user)
                }
            }

            binding.switchAdmin.setOnCheckedChangeListener { _, isChecked ->
                if (user.isAdmin != isChecked) {
                    user.isAdmin = isChecked
                    onUserStatusChanged(user)
                }
            }
        }
    }
}