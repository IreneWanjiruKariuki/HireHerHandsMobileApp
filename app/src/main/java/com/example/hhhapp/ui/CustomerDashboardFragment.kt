package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.hhhapp.databinding.FragmentCustomerDashboardBinding
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDashboardFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    private var _binding: FragmentCustomerDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        userViewModel.getUserById(userId)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvWelcome.text = "Welcome, ${user.userName}!"

                // Only female users can be workers
                if (user.userGender == "Female") {
                    android.util.Log.d("CustomerDashboard", "User is female, checking worker profile")
                    // Check worker profile status
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val workerProfile = db.WorkerProfileDao().getProfileByWorkerId(userId)
                        android.util.Log.d("CustomerDashboard", "Worker profile: $workerProfile")
                        android.util.Log.d("CustomerDashboard", "Worker status: ${workerProfile?.status}")

                        withContext(Dispatchers.Main) {
                            when {
                                workerProfile == null -> {
                                    android.util.Log.d("CustomerDashboard", "No worker profile found - showing Apply button")
                                    // No application yet - show Apply button
                                    binding.btnApplyWorker.visibility = View.VISIBLE
                                    binding.btnApplyWorker.text = "Apply as Worker"
                                    binding.btnApplyWorker.isEnabled = true
                                    binding.btnApplyWorker.setOnClickListener {
                                        parentFragmentManager.beginTransaction()
                                            .replace(R.id.fragmentContainer, ApplyAsWorkerFragment())
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                    binding.btnWorkerDashboard.visibility = View.GONE
                                    binding.tvWorkerStatus.visibility = View.GONE
                                }
                                workerProfile.status == "Pending" -> {
                                    android.util.Log.d("CustomerDashboard", "Status is Pending - showing pending message")
                                    // Application pending
                                    binding.btnApplyWorker.visibility = View.GONE
                                    binding.btnWorkerDashboard.visibility = View.GONE
                                    binding.tvWorkerStatus.visibility = View.VISIBLE
                                    binding.tvWorkerStatus.text = "⏳ Worker Application Pending Review"
                                    binding.tvWorkerStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                                }
                                workerProfile.status == "Approved" -> {
                                    android.util.Log.d("CustomerDashboard", "Status is Approved - showing Worker Dashboard button")
                                    // Approved - show Worker Dashboard button
                                    binding.btnApplyWorker.visibility = View.GONE
                                    binding.tvWorkerStatus.visibility = View.GONE
                                    binding.btnWorkerDashboard.visibility = View.VISIBLE
                                    binding.btnWorkerDashboard.setOnClickListener {
                                        parentFragmentManager.beginTransaction()
                                            .replace(R.id.fragmentContainer, WorkerDashboardFragment())
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                }
                                workerProfile.status == "Rejected" -> {
                                    android.util.Log.d("CustomerDashboard", "Status is Rejected - showing reapply option")
                                    // Rejected - show message and allow reapplication
                                    binding.btnWorkerDashboard.visibility = View.GONE
                                    binding.tvWorkerStatus.visibility = View.VISIBLE
                                    binding.tvWorkerStatus.text = "❌ Application Rejected. You can apply again."
                                    binding.tvWorkerStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))

                                    binding.btnApplyWorker.visibility = View.VISIBLE
                                    binding.btnApplyWorker.text = "Reapply as Worker"
                                    binding.btnApplyWorker.isEnabled = true
                                    binding.btnApplyWorker.setOnClickListener {
                                        parentFragmentManager.beginTransaction()
                                            .replace(R.id.fragmentContainer, ApplyAsWorkerFragment())
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    android.util.Log.d("CustomerDashboard", "User is male - hiding worker UI")
                    // Male users - hide all worker-related UI
                    binding.btnApplyWorker.visibility = View.GONE
                    binding.btnWorkerDashboard.visibility = View.GONE
                    binding.tvWorkerStatus.visibility = View.GONE
                }
            }
        }

        binding.btnPostJob.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PostJobFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnViewMyJobs.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CustomerJobsTabFragment())
                .addToBackStack(null)
                .commit()
        }

        // NEW: My Profile button
        binding.btnMyProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, UserProfileFragment())
                .addToBackStack(null)
                .commit()
        }

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