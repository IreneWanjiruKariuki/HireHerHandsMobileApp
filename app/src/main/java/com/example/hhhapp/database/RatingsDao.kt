package com.example.hhhapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RatingsDao {
    @Insert
    suspend fun insertRating(rating: Ratings)

    @Query("SELECT * FROM Ratings WHERE worker_id = :workerId")
    suspend fun getRatingsForWorker(workerId: Int): List<Ratings>
}