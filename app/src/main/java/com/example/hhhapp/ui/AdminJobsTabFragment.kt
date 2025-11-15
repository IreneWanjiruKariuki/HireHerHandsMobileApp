package com.example.hhhapp.ui

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

class AdminJobsTabFragment : Fragment() {

    private lateinit var btnPending: Button
    private lateinit var btnAccepted: Button
    private lateinit var btnCompleted: Button
    private lateinit var btnAll: Button
    private lateinit var jobsContainer: LinearLayout
    private lateinit var btnBack: Button

    private var currentTab = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_jobs_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPending = view.findViewById(R.id.btnPending)
        btnAccepted = view.findViewById(R.id.btnAccepted)
        btnCompleted = view.findViewById(R.id.btnCompleted)
        btnAll = view.findViewById(R.id.btnAll)
        jobsContainer = view.findViewById(R.id.jobsContainer)
        btnBack = view.findViewById(R.id.btnBack)

        // Set up tab buttons
        btnPending.setOnClickListener {
            currentTab = "PENDING"
            updateTabUI()
            loadJobs()
        }

        btnAccepted.setOnClickListener {
            currentTab = "ACCEPTED"
            updateTabUI()
            loadJobs()
        }

        btnCompleted.setOnClickListener {
            currentTab = "COMPLETED"
            updateTabUI()
            loadJobs()
        }

        btnAll.setOnClickListener {
            currentTab = "ALL"
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
        btnAccepted.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        btnCompleted.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        btnAll.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))

        // Highlight active tab
        when (currentTab) {
            "PENDING" -> btnPending.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "ACCEPTED" -> btnAccepted.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "COMPLETED" -> btnCompleted.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "ALL" -> btnAll.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }
    }

    private fun loadJobs() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val allJobs = db.JobDao().getAllJobs()

            val filteredJobs = when (currentTab) {
                "PENDING" -> allJobs.filter {
                    it.jobStatus == "PENDING_SELECTION" ||
                            it.jobStatus == "PENDING_WORKER_APPROVAL"
                }
                "ACCEPTED" -> allJobs.filter {
                    it.jobStatus == "ACCEPTED" ||
                            it.jobStatus == "ONGOING"
                }
                "COMPLETED" -> allJobs.filter {
                    it.jobStatus == "COMPLETED" ||
                            it.jobStatus == "PAID"
                }
                "ALL" -> allJobs
                else -> emptyList()
            }

            withContext(Dispatchers.Main) {
                jobsContainer.removeAllViews()

                if (filteredJobs.isEmpty()) {
                    val tv = TextView(requireContext())
                    tv.text = when (currentTab) {
                        "PENDING" -> "No pending jobs"
                        "ACCEPTED" -> "No active jobs"
                        "COMPLETED" -> "No completed jobs"
                        "ALL" -> "No jobs in the system"
                        else -> "No jobs"
                    }
                    tv.textSize = 16f
                    tv.setPadding(16, 16, 16, 16)
                    jobsContainer.addView(tv)
                } else {
                    for (job in filteredJobs) {
                        val customer = db.UserDao().getUserById(job.customerId)
                        val worker = if (job.workerId != null) db.UserDao().getUserById(job.workerId!!) else null
                        displayJobCard(job, customer, worker)
                    }
                }
            }
        }
    }

    private fun displayJobCard(job: Job, customer: User?, worker: User?) {
        val card = layoutInflater.inflate(R.layout.item_admin_job_card, jobsContainer, false)

        card.findViewById<TextView>(R.id.tvJobTitle).text = job.jobTitle
        card.findViewById<TextView>(R.id.tvJobDescription).text = job.jobDescription
        card.findViewById<TextView>(R.id.tvJobLocation).text = "Location: ${job.jobLocation}"
        card.findViewById<TextView>(R.id.tvJobDate).text = "Date: ${job.jobDate}"
        card.findViewById<TextView>(R.id.tvJobBudget).text = "Budget: KSh ${job.jobBudget}"
        card.findViewById<TextView>(R.id.tvJobStatus).text = "Status: ${job.jobStatus}"
        card.findViewById<TextView>(R.id.tvCustomerName).text = "Customer: ${customer?.userName ?: "Unknown"}"
        card.findViewById<TextView>(R.id.tvCustomerEmail).text = "Email: ${customer?.userEmail ?: "N/A"}"

        val workerInfo = if (worker != null) {
            "Worker: ${worker.userName} (${worker.userEmail})"
        } else {
            "Worker: Not assigned"
        }
        card.findViewById<TextView>(R.id.tvWorkerInfo).text = workerInfo

        jobsContainer.addView(card)
    }
}