package com.example.hhhapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =
    [User ::class, Job ::class, Skills ::class, Ratings ::class, WorkerProfile ::class ],
    version = 1)
abstract class HireHerHandsDatabase: RoomDatabase() {

    //Connect HHHDatabase to the Dao Interface
    abstract fun UserDao(): UserDao
    abstract fun JobDao(): JobDao
    abstract fun SkillsDao(): SkillsDao
    abstract fun RatingsDao(): RatingsDao
    abstract fun WorkerProfileDao(): WorkerProfileDao

    //Creating a singleton instance
    companion object{
        @Volatile
        private var INSTANCE: HireHerHandsDatabase?= null

        fun getDatabase (context: Context): HireHerHandsDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HireHerHandsDatabase::class.java,
                    "hireherhands_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}