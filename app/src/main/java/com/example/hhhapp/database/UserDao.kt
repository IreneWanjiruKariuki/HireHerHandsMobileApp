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
    suspend fun login(email: String, password: Int): User?

    @Query("SELECT * FROM Users WHERE user_role = 'worker'")
    suspend fun getAllWorkers(): List<User>

    @Query("SELECT * FROM Users WHERE user_role = 'client'")
    suspend fun getClients(): List<User>

    @Update
    suspend fun updateUser (user:User)
}