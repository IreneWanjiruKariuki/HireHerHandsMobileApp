package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hhhapp.databinding.FragmentCustomerDashboardBinding
import com.example.hhhapp.R

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

        //get the user's ID from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)


        // ask ViewModel to load that user's info from the DB
        userViewModel.getUserById(userId)

        // observe the LiveData and update the UI when data arrives
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvWelcome.text = "Welcome, ${user.userName}!"

                //if user = female they can apply to be a worker
                if (user.userGender == "Female") {
                    binding.btnApplyWorker.visibility = View.VISIBLE
                    binding.btnApplyWorker.setOnClickListener {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ApplyAsWorkerFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    binding.btnApplyWorker.visibility = View.GONE
                }
            }

        }

        binding.btnPostJob.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PostJobFragment())
                .addToBackStack(null)
                .commit()
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
