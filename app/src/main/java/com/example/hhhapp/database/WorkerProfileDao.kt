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

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Pending'")
    suspend fun getPendingWorkerProfiles(): List<WorkerProfile>

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Approved'")
    suspend fun getApprovedWorkerProfiles(): List<WorkerProfile>

    @Query("SELECT * FROM WorkerProfile WHERE status = 'Rejected'")
    suspend fun getRejectedWorkerProfiles(): List<WorkerProfile>

    @Query("UPDATE WorkerProfile SET status = :newStatus WHERE profile_id = :profileId")
    suspend fun updateWorkerStatus(profileId: Int, newStatus: String)

    @Query("""
        SELECT wp.* FROM WorkerProfile AS wp
        INNER JOIN worker_skill_cross_ref AS ws ON wp.profile_id = ws.profile_id
        WHERE ws.skill_id = :skillId AND wp.status = 'Approved'
    """)
    suspend fun getApprovedWorkersBySkill(skillId: Int): List<WorkerProfile>
}