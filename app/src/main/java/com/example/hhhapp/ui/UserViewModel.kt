package com.example.hhhapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// vm acts as a middle layer between the fragments and the db
class UserViewModel(application: Application) : AndroidViewModel(application) {


    private val userDao = HireHerHandsDatabase.getDatabase(application).UserDao()

    //LiveData to store and observe current logged-in user
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    //LiveData to observe signup result messages
    private val _signupResult = MutableLiveData<String>()
    val signupResult: LiveData<String> get() = _signupResult

    //login running in the background
    fun loginUser(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.login(email, password)
            _loggedInUser.postValue(user)
        }
    }

    //signup running in the background
    fun signupUser(newUser: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = userDao.checkEmailExists(newUser.userEmail)
            if (existing != null) {
                _signupResult.postValue("Email already registered")
            } else {
                userDao.insertUser(newUser)
                _signupResult.postValue("Account created successfully!")
            }
        }
    }
    // LiveData to hold user details
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    //get user details by ID
    fun getUserById(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDetails = userDao.getUserById(userId)
            _user.postValue(userDetails)
        }
    }
    // For ApplyAsWorkerFragment — mark application pending
    private val _applicationResult = MutableLiveData<String>()
    val applicationResult: LiveData<String> get() = _applicationResult

    fun applyAsWorker(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.markWorkerApplicationPending(userId)
                _applicationResult.postValue("Application submitted successfully! Awaiting admin review.")
            } catch (e: Exception) {
                _applicationResult.postValue("Error submitting application.")
            }
        }
    }



}
