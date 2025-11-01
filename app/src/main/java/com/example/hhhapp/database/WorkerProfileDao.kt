package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkerProfileDao {
    @Insert
    suspend fun insertProfile(profile: WorkerProfile)

    @Update
    suspend fun updateProfile(profile: WorkerProfile)

    @Query("SELECT * FROM WorkerProfile WHERE worker_id = :workerId LIMIT 1")
    suspend fun getProfileByWorkerId(workerId: Int): WorkerProfile?
}