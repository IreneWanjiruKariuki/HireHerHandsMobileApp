package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class User(
    @PrimaryKey @ColumnInfo (name = "user_id") val userId:Int,
    @ColumnInfo (name = "user_name") val userName:String,
    @ColumnInfo (name = "user_role") val userRole:String,
    @ColumnInfo (name = "user_email") val userEmail: String,
    @ColumnInfo (name = "user_password") val userPassword:Int

    )
