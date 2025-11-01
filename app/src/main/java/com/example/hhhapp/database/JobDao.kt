package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface JobDao {

    @Insert
    suspend fun createJob (job: Job)

    @Update
    suspend fun updateJob (job: Job)

    @Query("SELECT * FROM Job WHERE customer_id = :customerId")
    suspend fun getJobsByCustomer(customerId: Int): List<Job>

    @Query("SELECT * FROM Job WHERE worker_id = :workerId")
    suspend fun getJobsByWorker(workerId: Int): List<Job>

    @Query("SELECT * FROM Job WHERE job_status = 'pending'")
    suspend fun getPendingJobs(): List<Job>
}