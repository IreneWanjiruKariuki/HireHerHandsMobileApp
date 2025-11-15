package com.example.hhhapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewWorkersFragment : Fragment() {

    private lateinit var workersContainer: LinearLayout
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_view_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvTitle).text = "All Approved Workers"
        workersContainer = view.findViewById(R.id.usersContainer)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Debug first to see what's in the database
        debugWorkerData()
        loadWorkers()
    }

    private fun debugWorkerData() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check all worker profiles
                val allProfiles = db.WorkerProfileDao().getAllWorkerProfiles()
                Log.d("DEBUG", "Total worker profiles: ${allProfiles.size}")

                allProfiles.forEach { profile ->
                    Log.d("DEBUG", "Profile: ID=${profile.profileId}, WorkerID=${profile.workerID}, Status=${profile.status}, Location=${profile.location}")
                }

                // Check approved workers specifically
                val approved = db.WorkerProfileDao().getApprovedWorkerProfiles()
                Log.d("DEBUG", "Approved workers: ${approved.size}")

                // Check pending workers
                val pending = db.WorkerProfileDao().getPendingWorkerProfiles()
                Log.d("DEBUG", "Pending workers: ${pending.size}")

            } catch (e: Exception) {
                Log.e("DEBUG", "Error debugging data", e)
            }
        }
    }

    private fun loadWorkers() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the correct DAO method
                val approvedProfiles = db.WorkerProfileDao().getApprovedWorkerProfiles()
                Log.d("AdminViewWorkers", "Found ${approvedProfiles.size} approved worker profiles")

                withContext(Dispatchers.Main) {
                    workersContainer.removeAllViews()

                    if (approvedProfiles.isEmpty()) {
                        val tv = TextView(requireContext())
                        tv.text = "No approved workers found"
                        tv.textSize = 16f
                        tv.setPadding(16, 16, 16, 16)
                        workersContainer.addView(tv)
                        Log.d("AdminViewWorkers", "No approved workers to display")
                    } else {
                        for (profile in approvedProfiles) {
                            displayWorkerCard(profile, db)
                        }
                        Log.d("AdminViewWorkers", "Displayed ${approvedProfiles.size} approved workers")
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminViewWorkers", "Error loading workers", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading workers: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayWorkerCard(profile: WorkerProfile, db: HireHerHandsDatabase) {
        val card = layoutInflater.inflate(R.layout.item_worker_details_card, workersContainer, false)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user details
                val user = db.UserDao().getUserById(profile.workerID)
                val jobs = db.JobDao().getJobsByWorker(profile.workerID)
                val completedJobs = jobs.count { it.jobStatus == "COMPLETED" || it.jobStatus == "PAID" }

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        card.findViewById<TextView>(R.id.tvUserName).text = "Name: ${user.userName}"
                        card.findViewById<TextView>(R.id.tvUserEmail).text = "Email: ${user.userEmail}"
                        card.findViewById<TextView>(R.id.tvUserGender).text = "Gender: ${user.userGender}"
                        card.findViewById<TextView>(R.id.tvUserId).text = "ID: ${user.userId}"
                    } else {
                        card.findViewById<TextView>(R.id.tvUserName).text = "Name: Unknown (User ID: ${profile.workerID})"
                        card.findViewById<TextView>(R.id.tvUserEmail).text = "Email: N/A"
                    }

                    // Display profile information
                    card.findViewById<TextView>(R.id.tvWorkerLocation).text = "Location: ${profile.location}"
                    card.findViewById<TextView>(R.id.tvWorkerRate).text = "Rate: KSh ${profile.hourlyRate}/hr"
                    card.findViewById<TextView>(R.id.tvWorkerExperience).text = "Experience: ${profile.experienceYears} years"
                    card.findViewById<TextView>(R.id.tvWorkerRating).text = "Rating: ${profile.averageRating}/5.0"
                    card.findViewById<TextView>(R.id.tvUserStats).text = "Jobs Completed: $completedJobs | Total Jobs: ${jobs.size}"

                    workersContainer.addView(card)
                }
            } catch (e: Exception) {
                Log.e("AdminViewWorkers", "Error displaying worker card for profile ${profile.profileId}", e)
            }
        }
    }
}