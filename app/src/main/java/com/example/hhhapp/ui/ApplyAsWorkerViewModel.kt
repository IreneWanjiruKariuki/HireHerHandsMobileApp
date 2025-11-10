package com.example.hhhapp.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
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
        location: String
    ) {
        val profile = WorkerProfile(
            workerID = workerId,
            workerBio = bio,
            averageRating = 0.0,
            hourlyRate = hourlyRate,
            location = location,
            experienceYears = experience,
            status = "Pending"
        )

        // Insert using coroutine (safe for suspend DAO functions)
        viewModelScope.launch(Dispatchers.IO) {
            db.WorkerProfileDao().insertProfile(profile)
            _message.postValue("Application submitted successfully!")
        }
    }
}

