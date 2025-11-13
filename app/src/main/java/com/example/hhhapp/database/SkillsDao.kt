package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SkillsDao {

    @Insert
    suspend fun insertSkill(skill: Skills)

    @Insert
    suspend fun insertAll(skills: List<Skills>)


    @Update
    suspend fun updateSkill(skill: Skills)

    @Query("SELECT * FROM Skills ORDER BY skill_name ASC")
    suspend fun getAllSkills(): List<Skills>

    @Query("SELECT * FROM skills WHERE skill_id = :id LIMIT 1")
    suspend fun getSkillById(id: Int): Skills?
}