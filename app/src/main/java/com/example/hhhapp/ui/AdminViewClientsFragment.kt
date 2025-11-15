package com.example.hhhapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminViewClientsFragment : Fragment() {

    private lateinit var clientsContainer: LinearLayout
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

        view.findViewById<TextView>(R.id.tvTitle).text = "All Clients"
        clientsContainer = view.findViewById(R.id.usersContainer)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadClients()
    }

    private fun loadClients() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            // Get all users who are not workers (not approved and not pending)
            val allUsers = db.UserDao().getAllUsers()
            val clients = allUsers.filter { !it.isWorkerApproved && !it.isWorkerPending }

            withContext(Dispatchers.Main) {
                clientsContainer.removeAllViews()

                if (clients.isEmpty()) {
                    val tv = TextView(requireContext())
                    tv.text = "No clients found"
                    tv.textSize = 16f
                    tv.setPadding(16, 16, 16, 16)
                    clientsContainer.addView(tv)
                } else {
                    for (client in clients) {
                        displayClientCard(client, db)
                    }
                }
            }
        }
    }

    private fun displayClientCard(client: User, db: HireHerHandsDatabase) {
        val card = layoutInflater.inflate(R.layout.item_user_card, clientsContainer, false)

        card.findViewById<TextView>(R.id.tvUserName).text = "Name: ${client.userName}"
        card.findViewById<TextView>(R.id.tvUserEmail).text = "Email: ${client.userEmail}"
        card.findViewById<TextView>(R.id.tvUserGender).text = "Gender: ${client.userGender}"
        card.findViewById<TextView>(R.id.tvUserId).text = "ID: ${client.userId}"

        // Get job stats
        CoroutineScope(Dispatchers.IO).launch {
            val jobs = db.JobDao().getJobsByCustomer(client.userId)
            val jobCount = jobs.size
            val completedJobs = jobs.count { it.jobStatus == "COMPLETED" || it.jobStatus == "PAID" }

            withContext(Dispatchers.Main) {
                card.findViewById<TextView>(R.id.tvUserStats).text =
                    "Jobs Posted: $jobCount | Completed: $completedJobs"
            }
        }

        clientsContainer.addView(card)
    }
}