package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.databinding.FragmentWorkerDashboardBinding
import com.example.hhhapp.R

class WorkerDashboardFragment : Fragment() {

    //viewmodel instance
    private val userViewModel: UserViewModel by viewModels()

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get userId from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)

        //ask ViewModel to load the user info
        userViewModel.getUserById(userId)

        //observe LiveData to update UI
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvWelcome.text = "Welcome, ${user.userName}!)"
            } else {
                binding.tvWelcome.text = "User not found."
            }
        }

        /*view Available Jobs button
        binding.btnViewJobs.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WorkerJobsFragment())
                .addToBackStack(null)
                .commit()
        }
         */

        //view My Accepted Jobs button
        binding.btnMyJobs.setOnClickListener {
            // TODO: Create MyJobsFragment to show worker's accepted jobs
            Toast.makeText(requireContext(), "My Jobs feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        //logout button
        binding.btnLogout.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

