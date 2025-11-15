package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.R

class AdminDashboardFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var tvWelcome: TextView
    private lateinit var btnManageWorkers: Button
    private lateinit var btnViewJobs: Button
    private lateinit var btnViewClients: Button
    private lateinit var btnViewWorkers: Button
    private lateinit var btnManageSkills: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcome = view.findViewById(R.id.tvWelcome)
        btnManageWorkers = view.findViewById(R.id.btnManageWorkers)
        btnViewJobs = view.findViewById(R.id.btnViewJobs)
        btnViewClients = view.findViewById(R.id.btnViewClients)
        btnViewWorkers = view.findViewById(R.id.btnViewWorkers)
        btnManageSkills = view.findViewById(R.id.btnManageSkills)
        btnLogout = view.findViewById(R.id.btnLogout)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)

        userViewModel.getUserById(userId)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvWelcome.text = "Welcome, ${user.userName}!"
            } else {
                tvWelcome.text = "Admin Dashboard"
            }
        }

        // Review Worker Applications
        btnManageWorkers.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminApproveWorkersFragment())
                .addToBackStack(null)
                .commit()
        }

        // View Jobs by Status
        btnViewJobs.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminJobsTabFragment())
                .addToBackStack(null)
                .commit()
        }

        // View All Clients
        btnViewClients.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminViewClientsFragment())
                .addToBackStack(null)
                .commit()
        }

        // View All Workers
        btnViewWorkers.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminViewWorkersFragment())
                .addToBackStack(null)
                .commit()
        }

        // Manage Skills
        btnManageSkills.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminManageSkillsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Logout
        btnLogout.setOnClickListener {
            with(sharedPref.edit()) {
                clear()
                apply()
            }
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }
    }
}
