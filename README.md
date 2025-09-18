# ChatUp - WhatsApp-like Minimal Chat App

A minimal WhatsApp-like Android application built using **Kotlin** and **Firebase**, supporting one-to-one and group chat (text + audio), user authentication, and admin-controlled group/user management.

---

## 🔧 Features

- 🔐 User Signup & Login with Email & Password  
- 💬 One-to-One Messaging  
- 👥 Group Chat (Admin-only group creation)  
- 🔊 Audio Message Support  
- 🛑 Admin Can Deactivate Users  
- 🚫 Deactivated Users are Logged Out Immediately and Prevented from Logging Back In  
- ✅ Reactivated Users Can Resume Access  

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **UI:** Android XML with Material Components  
- **Backend:** Firebase (Authentication, Firestore, Storage)  
- **Architecture:** MVVM (basic structure)

---

## 👨‍💻 Admin Panel

Accessible only to the app admin. Features:
- View all users
- Activate / Deactivate users
- Create and manage group chats

---

## 🔐 Authentication & Authorization

- Firebase Auth used for secure email/password login
- Firestore stores user roles (`admin` / `user`) and activation status
- Role-based UI and functionality access

---

## 🚀 Getting Started

### 📋 Prerequisites

- Android Studio (Flamingo or newer)
- Firebase Project with:
  - Firebase Authentication (Email/Password)
  - Firestore Database
  - Firebase Storage (for audio messages)
- Google Services JSON (`google-services.json`) in `app/` folder

### 🔌 Setup Instructions

1. **Clone the repository:**

```bash
git clone https://github.com/your-username/chatup.git
cd chatup
