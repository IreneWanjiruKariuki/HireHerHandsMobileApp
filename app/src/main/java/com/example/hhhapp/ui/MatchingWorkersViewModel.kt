package com.example.hhhapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import com.example.hhhapp.database.WorkerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchingWorkersViewModel : ViewModel() {

    companion object {
        private const val TAG = "MatchingWorkersVM"
    }

    private val _workers = MutableLiveData<List<Pair<WorkerProfile, User>>>()
    val workers: LiveData<List<Pair<WorkerProfile, User>>> get() = _workers

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun loadMatchingWorkers(database: HireHerHandsDatabase, skillId: Int) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "loadMatchingWorkers called with skillId: $skillId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // The DAO already has the correct query with INNER JOIN
                Log.d(TAG, "Querying database with getApprovedWorkersBySkill($skillId)")
                val workerProfiles = database.WorkerProfileDao().getApprovedWorkersBySkill(skillId)

                Log.d(TAG, "Query returned ${workerProfiles.size} approved worker profiles")

                if (workerProfiles.isEmpty()) {
                    Log.w(TAG, "No approved workers found for skillId: $skillId")

                    // Debug: Check if there are ANY profiles in cross-ref table
                    val crossRefDao = database.WorkerSkillCrossRefDao()
                    val allProfileIds = crossRefDao.getProfileIdsForSkill(skillId)
                    Log.d(TAG, "Cross-ref table has ${allProfileIds.size} profile_ids for this skill: $allProfileIds")

                    // Debug: Check if there are ANY approved profiles at all
                    val allApproved = database.WorkerProfileDao().getApprovedWorkerProfiles()
                    Log.d(TAG, "Total approved profiles in database: ${allApproved.size}")

                    _workers.postValue(emptyList())
                    return@launch
                }

                // Get user details for each profile
                val userDao = database.UserDao()
                val fullProfiles = mutableListOf<Pair<WorkerProfile, User>>()

                for (profile in workerProfiles) {
                    Log.d(TAG, "Profile found: profileId=${profile.profileId}, workerId=${profile.workerID}, status=${profile.status}")

                    val user = userDao.getUserById(profile.workerID)
                    if (user != null) {
                        Log.d(TAG, "  -> User found: ${user.userName} (userId=${user.userId})")
                        fullProfiles.add(Pair(profile, user))
                    } else {
                        Log.w(TAG, "  -> User NOT FOUND for workerID: ${profile.workerID}")
                    }
                }

                Log.d(TAG, "Total profiles to display: ${fullProfiles.size}")
                Log.d(TAG, "========================================")
                _workers.postValue(fullProfiles)

            } catch (e: Exception) {
                Log.e(TAG, "ERROR loading workers: ${e.message}", e)
                e.printStackTrace()
                _message.postValue("Error loading workers: ${e.message}")
            }
        }
    }
}