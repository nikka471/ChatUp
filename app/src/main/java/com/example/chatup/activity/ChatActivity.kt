package com.example.chatup.activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatup.adapters.ChatAdapter
import com.example.chatup.databinding.ActivityChatBinding
import com.example.chatup.models.Message
import com.example.chatup.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private lateinit var otherUserId: String

    private var otherUserIsActive: Boolean = true
    private val messagesList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private var messagesListener: ListenerRegistration? = null
    private var currentUserStatusListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        otherUserId = intent.getStringExtra("otherUserId") ?: ""

        if (otherUserId.isEmpty()) {
            Toast.makeText(this, "User data missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Generate chatId
        chatId = if (currentUserId < otherUserId) {
            "${currentUserId}_$otherUserId"
        } else {
            "${otherUserId}_$currentUserId"
        }

        // Listen to current user status in real-time
        listenToCurrentUserStatus()

        // Check if the other user is active and then proceed
        checkOtherUserStatus()
    }

    private fun listenToCurrentUserStatus() {
        currentUserStatusListener = db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    val isActive = snapshot.getBoolean("isActive") ?: true
                    if (!isActive) {
                        Toast.makeText(this, "Your account has been deactivated by admin", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        finish()
                    }
                }
            }
    }

    private fun checkOtherUserStatus() {
        db.collection("users").document(otherUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        if (user.isActive == true) {
                            otherUserIsActive = true
                            setupRecyclerView()
                            listenForMessages()
                            setupSendButton()
                        } else {
                            Toast.makeText(this, "This user is deactivated by admin", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "User data is corrupted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messagesList, currentUserId)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun listenForMessages() {
        messagesListener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading messages: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    messagesList.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(Message::class.java)
                        message?.let { messagesList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                    binding.rvMessages.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (TextUtils.isEmpty(messageText)) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Final check before sending
            if (!otherUserIsActive) {
                Toast.makeText(this, "Cannot send message. User is inactive.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendMessage(messageText)
        }
    }

    private fun sendMessage(text: String) {
        val message = Message(
            senderId = currentUserId,
            text = text,
            timestamp = Timestamp.now()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                binding.etMessage.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
        currentUserStatusListener?.remove()
    }
}