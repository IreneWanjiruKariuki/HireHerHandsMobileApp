package com.example.hhhapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities =
    [User ::class, Job ::class, Skills ::class, Ratings ::class, WorkerProfile ::class , WorkerSkillCrossRef::class],
    version = 6)
abstract class HireHerHandsDatabase: RoomDatabase() {

    //Connect HHHDatabase to the Dao Interface
    abstract fun UserDao(): UserDao
    abstract fun JobDao(): JobDao
    abstract fun SkillsDao(): SkillsDao
    abstract fun RatingsDao(): RatingsDao
    abstract fun WorkerProfileDao(): WorkerProfileDao
    abstract fun WorkerSkillCrossRefDao(): WorkerSkillCrossRefDao

    //Creating a singleton instance
    companion object {
        @Volatile
        private var INSTANCE: HireHerHandsDatabase? = null
        fun getDatabase(context: Context): HireHerHandsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HireHerHandsDatabase::class.java,
                    "hire_her_hands_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val database = INSTANCE ?: return
                            CoroutineScope(Dispatchers.IO).launch {
                                database.SkillsDao().apply {
                                    insertSkill(Skills(1, "Plumbing"))
                                    insertSkill(Skills(2, "Cleaning"))
                                    insertSkill(Skills(3, "Electrician"))
                                    insertSkill(Skills(4, "Hairdressing"))
                                    insertSkill(Skills(5, "Mounting"))
                                    insertSkill(Skills(6, "Painting"))
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance

            }
        }
    }
}