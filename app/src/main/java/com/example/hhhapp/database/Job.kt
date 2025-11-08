package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Job")
data class Job(
    @PrimaryKey(autoGenerate = true) @ColumnInfo (name = "job_id") val jobId: Int = 0,
    @ColumnInfo (name = "job_title") val jobTitle: String,
    @ColumnInfo (name = "job_description") val jobDescription: String,
    @ColumnInfo (name = "job_location") val jobLocation: String,
    @ColumnInfo (name = "job_date") val jobDate: String,
    @ColumnInfo(name = "job_budget") val jobBudget: Double,
    @ColumnInfo (name = "job_status") val jobStatus: String = "PENDING_SELECTION", // PENDING_SELECTION, PENDING_WORKER_APPROVAL, ACCEPTED, ONGOING, COMPLETED, PENDING_PAYMENT, PAID, REJECTED
    @ColumnInfo(name = "customer_id") val customerId: Int,
    @ColumnInfo(name = "worker_id") val workerId: Int?, //nullable until the worker accepts the job
    @ColumnInfo(name = "skill_id") val skillId: Int, // FK to Skill
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()

)
