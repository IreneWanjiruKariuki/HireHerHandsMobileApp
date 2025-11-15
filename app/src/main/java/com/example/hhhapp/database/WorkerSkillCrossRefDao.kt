package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkerSkillCrossRefDao {
    @Insert
    suspend fun insert(crossRef: WorkerSkillCrossRef)

    @Insert
    suspend fun insertAll(refs: List<WorkerSkillCrossRef>)

    @Query("SELECT profile_id FROM worker_skill_cross_ref WHERE skill_id = :skillId")
    suspend fun getProfileIdsForSkill(skillId: Int): List<Int>

    @Query("SELECT skill_id FROM worker_skill_cross_ref WHERE profile_id = :profileId")
    suspend fun getSkillIdsForProfile(profileId: Int): List<Int>

    @Query("DELETE FROM worker_skill_cross_ref WHERE skill_id = :skillId")
    suspend fun deleteBySkillId(skillId: Int)

    @Query("DELETE FROM worker_skill_cross_ref WHERE profile_id = :profileId")
    suspend fun deleteByProfileId(profileId: Int)
}
