package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface JobDao {

    @Insert
    suspend fun createJob (job: Job): Long

    @Query("SELECT * FROM Job")
    suspend fun getAllJobs(): List<Job>

    @Update
    suspend fun updateJob (job: Job)

    @Query("SELECT * FROM Job WHERE customer_id = :customerId")
    suspend fun getJobsByCustomer(customerId: Int): List<Job>

    @Query("SELECT * FROM Job WHERE worker_id = :workerId")
    suspend fun getJobsByWorker(workerId: Int): List<Job>

    @Query("SELECT * FROM Job WHERE job_status = 'PENDING_SELECTION'")
    suspend fun getPendingJobs(): List<Job>

    @Query("SELECT * FROM Job WHERE LOWER(job_location) LIKE LOWER(:location)")
    suspend fun getJobsByLocation(location: String): List<Job>

    @Query("UPDATE Job SET job_status = :newStatus, worker_id = :workerId WHERE job_id = :jobId")
    suspend fun updateJobStatus(jobId: Int, newStatus: String, workerId: Int?)
}