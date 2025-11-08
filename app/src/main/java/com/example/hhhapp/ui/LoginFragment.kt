package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.R
import com.example.hhhapp.databinding.FragmentLoginBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider


class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //viewmodel instance
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup? , savedInstanceState: Bundle?):
    // Initialize binding object
    View{
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //observe LiveData from the vm
        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                saveLoginState(user.userId)

                Toast.makeText(requireContext(), "Welcome ${user.userName}!", Toast.LENGTH_SHORT).show()

                if (user.userEmail == "admin@hireherhands.com") {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, AdminDashboardFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, CustomerDashboardFragment())
                        .addToBackStack(null)
                        .commit()
                }


                clearFields()
            } else {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }


        //Login button implementation
        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                userViewModel.loginUser(email, password)
            }
        }

        //Go to signup button implementation
        binding.goToSignupBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun saveLoginState(userId: Int) {
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            putInt("userId", userId)
            apply()
        }
    }


    private fun clearFields() {
        binding.email.text.clear()
        binding.password.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}