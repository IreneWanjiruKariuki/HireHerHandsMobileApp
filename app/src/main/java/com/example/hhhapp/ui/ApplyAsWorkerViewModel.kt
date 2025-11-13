package com.example.hhhapp.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import com.example.hhhapp.database.WorkerSkillCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplyAsWorkerViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // Store selected ID picture URI
    var idPictureUri: Uri? = null

    fun submitApplication(
        db: HireHerHandsDatabase,
        workerId: Int,
        bio: String,
        hourlyRate: Double,
        experience: Int,
        location: String,
        skillIds: List<Int>,
        idPictureUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ApplyWorkerVM", "Submitting application for workerId=$workerId")

                // CRITICAL FIX: Delete any existing profiles for this worker
                // This prevents duplicates and allows reapplication
                db.WorkerProfileDao().deleteAllProfilesForWorker(workerId)
                Log.d("ApplyWorkerVM", "Deleted old profiles for workerId=$workerId")

                // Create new profile
                val profile = WorkerProfile(
                    workerID = workerId,
                    workerBio = bio,
                    averageRating = 0.0,
                    hourlyRate = hourlyRate,
                    location = location,
                    experienceYears = experience,
                    status = "Pending",
                    idPictureUri = idPictureUri
                )

                // Insert the worker profile
                val profileId = db.WorkerProfileDao().insertProfile(profile).toInt()
                Log.d("ApplyWorkerVM", "Inserted new profile with profileId=$profileId")

                // Insert skills into WorkerSkillCrossRef
                skillIds.forEach { skillId ->
                    val crossRef = WorkerSkillCrossRef(
                        profileId = profileId,
                        skillId = skillId
                    )
                    db.WorkerSkillCrossRefDao().insert(crossRef)
                }
                Log.d("ApplyWorkerVM", "Inserted ${skillIds.size} skills")

                _message.postValue("Application submitted successfully!")
            } catch (e: Exception) {
                Log.e("ApplyWorkerVM", "Error submitting application", e)
                _message.postValue("Error: ${e.message}")
            }
        }
    }
}