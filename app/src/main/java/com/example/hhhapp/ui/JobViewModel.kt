package com.example.hhhapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//viewModel acts as a middle layer between Fragments and the DB
class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val jobDao = HireHerHandsDatabase.getDatabase(application).JobDao()

    //livedata for observing job lists and messages
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> get() = _jobs

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    //create a new job (posted by customer)
    private val _newJobId = MutableLiveData<Long>()
    val newJobId: LiveData<Long> get() = _newJobId

    fun postJob(job: Job) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = jobDao.createJob(job)
                _newJobId.postValue(id)
                _message.postValue("Job posted successfully!")
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            }
        }
    }

    // Reset job ID to prevent re-navigation
    fun resetJobId() {
        _newJobId.value = null
    }

    //load all jobs for a specific customer
    fun getJobsByCustomer(customerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = jobDao.getJobsByCustomer(customerId)
            _jobs.postValue(result)
        }
    }

    //load all pending jobs (for workers)
    fun getPendingJobs() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = jobDao.getPendingJobs()
            _jobs.postValue(result)
        }
    }

    //update job status (when a worker accepts or completes it)
    fun updateJobStatus(jobId: Int, newStatus: String, workerId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            jobDao.updateJobStatus(jobId, newStatus, workerId)
        }
    }

    //search jobs by location (case-insensitive)
    fun searchJobsByLocation(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = jobDao.getJobsByLocation("%$location%")
            _jobs.postValue(result)
        }
    }
}