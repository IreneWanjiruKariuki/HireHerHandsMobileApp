package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "WorkerProfile")
data class WorkerProfile(
    @PrimaryKey @ColumnInfo (name = "profile_id") val profileID : Int,
    @ColumnInfo(name = "worker_id") val workerID : Int,
    @ColumnInfo(name = "worker_bio") val workerBio : String,
    @ColumnInfo(name = "average_rating") val averageRating: Double,
    @ColumnInfo(name = "skills") val skills: String
)
