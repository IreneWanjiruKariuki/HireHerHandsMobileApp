package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser (user:User)

    @Query("SELECT * FROM Users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM Users WHERE user_id IN " +
            "(SELECT worker_id FROM WorkerProfile WHERE status = 'Approved')")
    suspend fun getApprovedWorkers(): List<User>

    @Query("SELECT * FROM Users WHERE user_email = :email AND user_password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM Users WHERE user_email = :email LIMIT 1")
    suspend fun checkEmailExists(email: String): User?

    @Query("SELECT * FROM Users WHERE user_id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM Users WHERE user_gender = :gender")
    suspend fun getUsersByGender(gender: String): List<User>

    @Query("SELECT * FROM Users WHERE is_worker_pending = 1 AND is_worker_approved = 0")
    suspend fun getPendingWorkerApplications(): List<User>

    //@Query("SELECT * FROM Users WHERE is_worker_approved = 1")
    //suspend fun getApprovedWorkers(): List<User>

    @Query("SELECT * FROM Users WHERE is_worker_pending = 0 AND is_worker_approved = 0")
    suspend fun getRejectedWorkers(): List<User>

    @Query("UPDATE Users SET is_worker_approved = :approved, is_worker_pending = 0 WHERE user_id = :userId")
    suspend fun updateWorkerApproval(userId: Int, approved: Boolean)

    @Query("UPDATE Users SET is_worker_pending = 1 WHERE user_id = :userId")
    suspend fun markWorkerApplicationPending(userId: Int)

    @Query("SELECT * FROM Users WHERE user_id IN (:ids) AND user_gender = 'Female' AND is_worker_approved = 1 ")
    suspend fun getApprovedWorkersByIds(ids: List<Int>): List<User>


    @Update
    suspend fun updateUser (user:User)
}