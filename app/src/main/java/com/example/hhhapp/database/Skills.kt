package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Skills")
data class Skills(
    @PrimaryKey @ColumnInfo(name = "skill_id") val skillId: Int,
    @ColumnInfo(name = "skill_name") val skillName: String
)

