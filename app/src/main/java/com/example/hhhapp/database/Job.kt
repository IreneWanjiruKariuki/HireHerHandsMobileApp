package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Job")
data class Job(
    @PrimaryKey @ColumnInfo (name = "job_id") val jobId: Int,
    @ColumnInfo (name = "job_title") val jobTitle: String,
    @ColumnInfo (name = "job_description") val jobDescription: Int,
    @ColumnInfo (name = "job_location") val jobLocation: String,
    @ColumnInfo (name = "job_date") val jobDate: String,
    @ColumnInfo (name = "job_status") val jobStatus: String,
    @ColumnInfo(name = "customer_id") val customerId: Int,
    @ColumnInfo(name = "worker_id") val workerId: Int? //nullable until the worker accepts the job

)
