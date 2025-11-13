package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var database: HireHerHandsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = HireHerHandsDatabase.getDatabase(this)

        // Insert hardcoded admin on first run
        insertAdminIfNotExists()

        // Check login state
        checkLoginState()
    }

    private fun insertAdminIfNotExists() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val adminEmail = "admin@hireherhands.com"
                val existingAdmin = database.UserDao().checkEmailExists(adminEmail)
                if (existingAdmin == null) {
                    val admin = User(
                        userId = 0,
                        userName = "Admin",
                        userEmail = adminEmail,
                        userPassword = "admin123",
                        userGender = "female"
                        // no role field
                    )
                    database.UserDao().insertUser(admin)
                }
            }
        }
    }

    private fun checkLoginState() {
        val sharedPref = getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        val fragment = if (isLoggedIn) {
            if (sharedPref.getBoolean("isAdmin", false)) {
                AdminDashboardFragment()
            } else {
                CustomerDashboardFragment()
            }
        } else {
            LoginFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
