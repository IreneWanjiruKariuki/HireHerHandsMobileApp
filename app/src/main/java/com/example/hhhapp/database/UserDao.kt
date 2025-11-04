package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser (user:User)

    @Query("SELECT * FROM Users WHERE user_email = :email AND user_password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM Users WHERE user_email = :email LIMIT 1")
    suspend fun checkEmailExists(email: String): User?

    @Query("SELECT * FROM Users WHERE user_role = 'worker'")
    suspend fun getAllWorkers(): List<User>

    @Query("SELECT * FROM Users WHERE user_role = 'client'")
    suspend fun getClients(): List<User>

    @Query("SELECT * FROM Users WHERE user_id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Update
    suspend fun updateUser (user:User)
}