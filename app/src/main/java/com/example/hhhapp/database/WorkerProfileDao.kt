package com.example.hhhapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkerProfileDao {
    @Insert
    suspend fun insertProfile(profile: WorkerProfile): Long

    @Update
    suspend fun updateProfile(profile: WorkerProfile)

    // FIXED: Get the MOST RECENT profile (highest profileId) for a worker
    @Query("SELECT * FROM WorkerProfile WHERE worker_id = :workerId ORDER BY profile_id DESC LIMIT 1")
    suspend fun getProfileByWorkerId(workerId: Int): WorkerProfile?

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Pending'")
    fun getPendingWorkerProfilesLive(): LiveData<List<WorkerProfile>>

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Approved'")
    suspend fun getApprovedWorkerProfiles(): List<WorkerProfile>

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Rejected'")
    suspend fun getRejectedWorkerProfiles(): List<WorkerProfile>

    @Query("UPDATE WorkerProfile SET status = :newStatus WHERE profile_id = :profileId")
    suspend fun updateWorkerStatus(profileId: Int, newStatus: String)

    @Query("SELECT wp.* FROM WorkerProfile AS wp INNER JOIN worker_skill_cross_ref AS ws ON wp.profile_id = ws.profile_id WHERE ws.skill_id = :skillId AND wp.status = 'Approved'")
    suspend fun getApprovedWorkersBySkill(skillId: Int): List<WorkerProfile>

    // ADDED: Check if worker already has a profile (for preventing duplicates)
    @Query("SELECT COUNT(*) FROM WorkerProfile WHERE worker_id = :workerId AND status IN ('Pending', 'Approved')")
    suspend fun hasActiveApplication(workerId: Int): Int

    // ADDED: Delete all profiles for a worker (for reapplication)
    @Query("DELETE FROM WorkerProfile WHERE worker_id = :workerId")
    suspend fun deleteAllProfilesForWorker(workerId: Int)
}