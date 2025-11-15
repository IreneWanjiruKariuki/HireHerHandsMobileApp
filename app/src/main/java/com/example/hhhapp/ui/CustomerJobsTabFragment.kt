package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
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
import com.example.hhhapp.database.Job
import com.example.hhhapp.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerJobsTabFragment : Fragment() {

    private lateinit var btnPending: Button
    private lateinit var btnActive: Button
    private lateinit var btnCompleted: Button
    private lateinit var jobsContainer: LinearLayout
    private lateinit var btnBack: Button

    private var currentTab = "PENDING_SELECTION"
    private var customerId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cutstomer_jobs_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPending = view.findViewById(R.id.btnPending)
        btnActive = view.findViewById(R.id.btnActive)
        btnCompleted = view.findViewById(R.id.btnCompleted)
        jobsContainer = view.findViewById(R.id.jobsContainer)
        btnBack = view.findViewById(R.id.btnBack)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        customerId = sharedPref.getInt("userId", -1)

        if (customerId == -1) {
            Toast.makeText(requireContext(), "Error: Customer not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Set up tab buttons
        btnPending.setOnClickListener {
            currentTab = "PENDING_SELECTION"
            updateTabUI()
            loadJobs()
        }

        btnActive.setOnClickListener {
            currentTab = "ACTIVE"
            updateTabUI()
            loadJobs()
        }

        btnCompleted.setOnClickListener {
            currentTab = "COMPLETED"
            updateTabUI()
            loadJobs()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Load initial tab
        updateTabUI()
        loadJobs()
    }

    private fun updateTabUI() {
        // Reset all buttons
        btnPending.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        btnActive.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        btnCompleted.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))

        // Highlight active tab
        when (currentTab) {
            "PENDING_SELECTION" -> btnPending.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "ACTIVE" -> btnActive.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "COMPLETED" -> btnCompleted.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }
    }

    private fun loadJobs() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allJobs = db.JobDao().getJobsByCustomer(customerId)

                val filteredJobs = when (currentTab) {
                    "PENDING_SELECTION" -> allJobs.filter {
                        it.jobStatus == "PENDING_SELECTION" || it.jobStatus == "PENDING_WORKER_APPROVAL"
                    }
                    "ACTIVE" -> allJobs.filter {
                        it.jobStatus == "ACCEPTED" || it.jobStatus == "ONGOING"
                    }
                    "COMPLETED" -> allJobs.filter {
                        it.jobStatus == "COMPLETED" || it.jobStatus == "PAID"
                    }
                    else -> emptyList()
                }

                withContext(Dispatchers.Main) {
                    jobsContainer.removeAllViews()

                    if (filteredJobs.isEmpty()) {
                        val tv = TextView(requireContext())
                        tv.text = when (currentTab) {
                            "PENDING_SELECTION" -> "No pending jobs"
                            "ACTIVE" -> "No active jobs"
                            "COMPLETED" -> "No completed jobs"
                            else -> "No jobs"
                        }
                        tv.textSize = 16f
                        tv.setPadding(16, 16, 16, 16)
                        jobsContainer.addView(tv)
                    } else {
                        for (job in filteredJobs) {
                            withContext(Dispatchers.IO) {
                                val worker = if (job.workerId != null) {
                                    db.UserDao().getUserById(job.workerId)
                                } else null
                                withContext(Dispatchers.Main) {
                                    displayJobCard(job, worker, db)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading jobs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayJobCard(job: Job, worker: User?, db: HireHerHandsDatabase) {
        if (currentTab == "ACTIVE" && job.jobStatus == "ONGOING") {
            // Jobs that can be marked as complete
            val card = layoutInflater.inflate(R.layout.item_customer_active_job_card, jobsContainer, false)

            // Set common job info
            card.findViewById<TextView>(R.id.tvJobTitle).text = job.jobTitle
            card.findViewById<TextView>(R.id.tvJobDescription).text = job.jobDescription
            card.findViewById<TextView>(R.id.tvJobLocation).text = "Location: ${job.jobLocation}"
            card.findViewById<TextView>(R.id.tvJobDate).text = "Date: ${job.jobDate}"
            card.findViewById<TextView>(R.id.tvJobBudget).text = "Budget: KSh ${job.jobBudget}"
            card.findViewById<TextView>(R.id.tvJobStatus).text = "Status: ${job.jobStatus}"

            // Set worker info - safely check if views exist
            val workerNameView = card.findViewById<TextView>(R.id.tvWorkerName)
            workerNameView.text = "Worker: ${worker?.userName ?: "Not assigned"}"

            val workerEmailView = card.findViewById<TextView>(R.id.tvWorkerEmail)
            workerEmailView.text = "Email: ${worker?.userEmail ?: "N/A"}"


            jobsContainer.addView(card)
        } else {
            // Regular job card
            val card = layoutInflater.inflate(R.layout.item_customer_job_card, jobsContainer, false)

            // Set common job info
            card.findViewById<TextView>(R.id.tvJobTitle).text = job.jobTitle
            card.findViewById<TextView>(R.id.tvJobDescription).text = job.jobDescription
            card.findViewById<TextView>(R.id.tvJobLocation).text = "Location: ${job.jobLocation}"
            card.findViewById<TextView>(R.id.tvJobDate).text = "Date: ${job.jobDate}"
            card.findViewById<TextView>(R.id.tvJobBudget).text = "Budget: KSh ${job.jobBudget}"
            card.findViewById<TextView>(R.id.tvJobStatus).text = "Status: ${job.jobStatus}"

            // Set worker name - safely check if view exists
            val workerNameView = card.findViewById<TextView>(R.id.tvWorkerName)
            workerNameView.text = "Worker: ${worker?.userName ?: "Not assigned yet"}"

            // Safely handle worker email if the view exists
            try {
                val workerEmailView = card.findViewById<TextView>(R.id.tvWorkerEmail)
                workerEmailView.text = "Email: ${worker?.userEmail ?: "N/A"}"
            } catch (e: Exception) {
                // tvWorkerEmail doesn't exist in this layout, that's okay
            }

            jobsContainer.addView(card)
        }
    }

    private fun markJobComplete(db: HireHerHandsDatabase, jobId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.JobDao().updateJobStatus(jobId, "COMPLETED", null)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Job marked as complete!", Toast.LENGTH_SHORT).show()
                loadJobs()
            }
        }
    }
}