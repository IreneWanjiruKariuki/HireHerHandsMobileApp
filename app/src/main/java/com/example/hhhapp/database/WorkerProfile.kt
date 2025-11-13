package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "WorkerProfile")
data class WorkerProfile(
    @PrimaryKey (autoGenerate = true)@ColumnInfo (name = "profile_id") val profileId : Int = 0,
    @ColumnInfo(name = "worker_id") val workerID : Int,
    @ColumnInfo(name = "worker_bio") val workerBio : String,
    @ColumnInfo(name = "average_rating") val averageRating: Double,
    @ColumnInfo(name = "hourly_rate") val hourlyRate: Double = 0.0,
    @ColumnInfo(name = "location") val location: String ,
    @ColumnInfo(name = "experience_years") val experienceYears: Int ,
    @ColumnInfo(name = "status") val status: String = "Pending", // "Pending", "Approved", or "Rejected"
    @ColumnInfo(name = "id_picture_uri")  val idPictureUri: String? = null

)
