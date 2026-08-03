package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.AppDatabase
import com.example.data.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()

    suspend fun login(phone: String, passwordHash: String): Long? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByPhone(phone)
        if (user != null && user.passwordHash == passwordHash) {
            return@withContext user.id
        }
        null
    }

    suspend fun register(phone: String, passwordHash: String): Long? = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) return@withContext null
        return@withContext userDao.insertUser(UserEntity(phone = phone, passwordHash = passwordHash))
    }

    suspend fun loginOrRegisterSms(phone: String): Long = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) {
            return@withContext existing.id
        }
        return@withContext userDao.insertUser(UserEntity(phone = phone))
    }
}
