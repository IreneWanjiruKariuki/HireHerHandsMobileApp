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

class WorkerJobsTabFragment : Fragment() {

    private lateinit var btnPending: Button
    private lateinit var btnAccepted: Button
    private lateinit var btnCompleted: Button
    private lateinit var jobsContainer: LinearLayout
    private lateinit var btnBack: Button

    private var currentTab = "PENDING_WORKER_APPROVAL"
    private var workerId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_jobs_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPending = view.findViewById(R.id.btnPending)
        btnAccepted = view.findViewById(R.id.btnAccepted)
        btnCompleted = view.findViewById(R.id.btnCompleted)
        jobsContainer = view.findViewById(R.id.jobsContainer)
        btnBack = view.findViewById(R.id.btnBack)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        workerId = sharedPref.getInt("userId", -1)

        if (workerId == -1) {
            Toast.makeText(requireContext(), "Error: Worker not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Set up tab buttons
        btnPending.setOnClickListener {
            currentTab = "PENDING_WORKER_APPROVAL"
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

        // Highlight active tab
        when (currentTab) {
            "PENDING_WORKER_APPROVAL" -> btnPending.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "ACCEPTED" -> btnAccepted.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
            "COMPLETED" -> btnCompleted.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }
    }

    private fun loadJobs() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val allJobs = db.JobDao().getJobsByWorker(workerId)

            val filteredJobs = when (currentTab) {
                "PENDING_WORKER_APPROVAL" -> allJobs.filter { it.jobStatus == "PENDING_WORKER_APPROVAL" }
                "ACCEPTED" -> allJobs.filter { it.jobStatus == "ACCEPTED" || it.jobStatus == "ONGOING" }
                "COMPLETED" -> allJobs.filter { it.jobStatus == "COMPLETED" || it.jobStatus == "PAID" }
                else -> emptyList()
            }

            withContext(Dispatchers.Main) {
                jobsContainer.removeAllViews()

                if (filteredJobs.isEmpty()) {
                    val tv = TextView(requireContext())
                    tv.text = when (currentTab) {
                        "PENDING_WORKER_APPROVAL" -> "No pending job requests"
                        "ACCEPTED" -> "No active jobs"
                        "COMPLETED" -> "No completed jobs"
                        else -> "No jobs"
                    }
                    tv.textSize = 16f
                    tv.setPadding(16, 16, 16, 16)
                    jobsContainer.addView(tv)
                } else {
                    for (job in filteredJobs) {
                        val customer = db.UserDao().getUserById(job.customerId)
                        displayJobCard(job, customer, db)
                    }
                }
            }
        }
    }

    private fun displayJobCard(job: Job, customer: User?, db: HireHerHandsDatabase) {
        if (currentTab == "PENDING_WORKER_APPROVAL") {
            // Use pending job card with Accept/Reject buttons
            val card = layoutInflater.inflate(R.layout.item_pending_job_card, jobsContainer, false)

            card.findViewById<TextView>(R.id.tvJobTitle).text = job.jobTitle
            card.findViewById<TextView>(R.id.tvJobDescription).text = job.jobDescription
            card.findViewById<TextView>(R.id.tvJobLocation).text = "Location: ${job.jobLocation}"
            card.findViewById<TextView>(R.id.tvJobDate).text = "Date: ${job.jobDate}"
            card.findViewById<TextView>(R.id.tvJobBudget).text = "Budget: KSh ${job.jobBudget}"
            card.findViewById<TextView>(R.id.tvJobStatus).text = "Status: ${job.jobStatus}"
            card.findViewById<TextView>(R.id.tvCustomerName).text = "Customer: ${customer?.userName ?: "Unknown"}"
            card.findViewById<TextView>(R.id.tvCustomerEmail).text = "Email: ${customer?.userEmail ?: "N/A"}"

            val btnAccept = card.findViewById<Button>(R.id.btnAcceptJob)
            val btnReject = card.findViewById<Button>(R.id.btnRejectJob)

            btnAccept.setOnClickListener {
                acceptJob(db, job.jobId, workerId)
            }

            btnReject.setOnClickListener {
                rejectJob(db, job.jobId)
            }

            jobsContainer.addView(card)
        } else {
            // Use regular job card without buttons
            val card = layoutInflater.inflate(R.layout.item_job_card, jobsContainer, false)

            card.findViewById<TextView>(R.id.tvJobTitle).text = job.jobTitle
            card.findViewById<TextView>(R.id.tvJobDescription).text = job.jobDescription
            card.findViewById<TextView>(R.id.tvJobLocation).text = "Location: ${job.jobLocation}"
            card.findViewById<TextView>(R.id.tvJobDate).text = "Date: ${job.jobDate}"
            card.findViewById<TextView>(R.id.tvJobBudget).text = "Budget: KSh ${job.jobBudget}"
            card.findViewById<TextView>(R.id.tvJobStatus).text = "Status: ${job.jobStatus}"
            card.findViewById<TextView>(R.id.tvCustomerName).text = "Customer: ${customer?.userName ?: "Unknown"}"
            card.findViewById<TextView>(R.id.tvCustomerEmail).text = "Email: ${customer?.userEmail ?: "N/A"}"

            jobsContainer.addView(card)
        }
    }

    private fun acceptJob(db: HireHerHandsDatabase, jobId: Int, workerId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.JobDao().updateJobStatus(jobId, "ACCEPTED", workerId)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Job accepted!", Toast.LENGTH_SHORT).show()
                loadJobs()
            }
        }
    }

    private fun rejectJob(db: HireHerHandsDatabase, jobId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.JobDao().updateJobStatus(jobId, "PENDING_SELECTION", null)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Job rejected", Toast.LENGTH_SHORT).show()
                loadJobs()
            }
        }
    }
}