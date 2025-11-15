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

class WorkerDashboardFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var tvWelcome: TextView
    private lateinit var btnViewJobs: Button
    private lateinit var btnMyProfile: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcome = view.findViewById(R.id.tvWelcome)
        btnViewJobs = view.findViewById(R.id.btnViewJobs)
        btnMyProfile = view.findViewById(R.id.btnMyProfile)
        btnLogout = view.findViewById(R.id.btnLogout)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)

        userViewModel.getUserById(userId)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvWelcome.text = "Welcome, ${user.userName}!"
            } else {
                tvWelcome.text = "User not found."
            }
        }

        // Navigate to jobs with tabs
        btnViewJobs.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WorkerJobsTabFragment())
                .addToBackStack(null)
                .commit()
        }

        // View worker profile
        btnMyProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WorkerProfileFragment())
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

