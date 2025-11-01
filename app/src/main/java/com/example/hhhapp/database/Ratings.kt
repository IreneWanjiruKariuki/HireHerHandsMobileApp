package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Ratings")
data class Ratings(
    @PrimaryKey @ColumnInfo (name = "rating_id")val ratingId: Int = 0,
    @ColumnInfo(name = "job_id") val jobId: Int,
    @ColumnInfo(name = "customer_id") val customerId: Int,
    @ColumnInfo(name = "worker_id") val workerId: Int,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "comment") val comment: String
)
