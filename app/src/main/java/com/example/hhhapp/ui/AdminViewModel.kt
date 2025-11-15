package com.example.hhhapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HireHerHandsDatabase.getDatabase(application)
    val pendingWorkers: LiveData<List<WorkerProfile>> = db.WorkerProfileDao().getPendingWorkerProfilesLive()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun approveWorker(worker: WorkerProfile) {
        Log.d("AdminViewModel", "approveWorker called for profileId=${worker.profileId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AdminViewModel", "Updating status to Approved for profileId=${worker.profileId}")
                db.WorkerProfileDao().updateWorkerStatus(worker.profileId, "Approved")

                // Verify the update
                val updatedWorker = db.WorkerProfileDao().getProfileByWorkerId(worker.workerID)
                Log.d("AdminViewModel", "After update, status is: ${updatedWorker?.status}")

                _message.postValue("Worker approved")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error approving worker", e)
                _message.postValue("Error approving worker: ${e.message}")
            }
        }
    }

    fun rejectWorker(worker: WorkerProfile) {
        Log.d("AdminViewModel", "rejectWorker called for profileId=${worker.profileId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("AdminViewModel", "Updating status to Rejected for profileId=${worker.profileId}")
                db.WorkerProfileDao().updateWorkerStatus(worker.profileId, "Rejected")

                // Verify the update
                val updatedWorker = db.WorkerProfileDao().getProfileByWorkerId(worker.workerID)
                Log.d("AdminViewModel", "After update, status is: ${updatedWorker?.status}")

                _message.postValue("Worker rejected")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error rejecting worker", e)
                _message.postValue("Error rejecting worker: ${e.message}")
            }
        }
    }
}

