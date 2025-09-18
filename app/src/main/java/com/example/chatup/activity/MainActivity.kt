package com.example.chatup.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatup.adapters.UserAdapter
import com.example.chatup.databinding.ActivityMainBinding
import com.example.chatup.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val usersList = mutableListOf<User>()
    private lateinit var userAdapter: UserAdapter

    private var currentUser: User? = null
    private var userStatusListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()

        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        listenToUserStatus(currentUid)

        loadCurrentUserAndUsers(currentUid)

        binding.btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(usersList) { user ->
            openChat(user)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun loadCurrentUserAndUsers(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)

                    // Show admin panel button only if current user is admin
                    binding.btnAdminPanel.visibility =
                        if (currentUser?.isAdmin == true) View.VISIBLE else View.GONE

                    loadUsers(uid)
                } else {
                    Toast.makeText(this, "Current user data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load current user", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadUsers(currentUid: String) {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                usersList.clear()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    // Add only active users and not the current user
                    if (user != null && user.uid != currentUid && (user.isActive ?: true)) {
                        usersList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openChat(user: User) {
        // Prevent chat with deactivated users
        if (user.isActive != true) {
            Toast.makeText(this, "This user is deactivated by admin", Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent chat if user data is missing
        if (user.uid.isNullOrEmpty() || user.email.isNullOrEmpty()) {
            Toast.makeText(this, "User data is incomplete or missing", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUserId", user.uid)
            putExtra("otherUserEmail", user.email)
        }
        startActivity(intent)
    }

    private fun listenToUserStatus(uid: String) {
        userStatusListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    val isActive = snapshot.getBoolean("isActive") ?: true
                    if (!isActive) {
                        auth.signOut()
                        Toast.makeText(this, "Your account has been deactivated by admin", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        userStatusListener?.remove()
    }
}