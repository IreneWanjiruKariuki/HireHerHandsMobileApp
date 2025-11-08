package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class User(
    @PrimaryKey(autoGenerate = true) @ColumnInfo (name = "user_id") val userId:Int,
    @ColumnInfo (name = "user_name") val userName:String,
    @ColumnInfo (name = "user_gender") val userGender: String ,
    @ColumnInfo (name = "user_email") val userEmail: String,
    @ColumnInfo (name = "user_password") val userPassword:String,
    @ColumnInfo (name = "is_worker_pending") val isWorkerPending :Boolean = false,
    @ColumnInfo (name = "is_worker_approved") val isWorkerApproved :Boolean = false

)
