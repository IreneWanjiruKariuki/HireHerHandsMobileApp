package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SkillsDao {

    @Insert
    suspend fun insertSkill(skill: Skills)

    @Update
    suspend fun updateSkill(skill: Skills)

    @Query("SELECT * FROM Skills")
    suspend fun getAllSkills(): List<Skills>
}