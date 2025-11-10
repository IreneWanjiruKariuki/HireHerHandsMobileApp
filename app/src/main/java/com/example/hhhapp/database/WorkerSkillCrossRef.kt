package com.example.hhhapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity

// creating a composite primary key
// this will show that this workers profile(profile_id) is lin
@Entity(tableName = "worker_skill_cross_ref", primaryKeys = ["profile_id", "skill_id"])
data class WorkerSkillCrossRef(
    @ColumnInfo(name = "profile_id") val profileId: Int,
    @ColumnInfo(name = "skill_id") val skillId: Int
)
