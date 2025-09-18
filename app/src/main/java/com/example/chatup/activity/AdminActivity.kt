package com.example.chatup.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatup.adapters.AdminUserAdapter
import com.example.chatup.databinding.ActivityAdminBinding
import com.example.chatup.models.User
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: FirebaseFirestore

    private val usersList = mutableListOf<User>()
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        adapter = AdminUserAdapter(usersList, ::onUserStatusChanged)
        binding.rvAdminUsers.layoutManager = LinearLayoutManager(this)
        binding.rvAdminUsers.adapter = adapter
    }

    private fun loadUsers() {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                usersList.clear()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    user?.let { usersList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onUserStatusChanged(user: User) {
        db.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show()
            }
    }
}