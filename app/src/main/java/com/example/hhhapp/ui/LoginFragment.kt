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
import com.example.hhhapp.database.User
import com.example.hhhapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe login result
        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                saveLoginState(user)

                if (user.userEmail == "admin@hireherhands.com") {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, AdminDashboardFragment())
                        .commit()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, CustomerDashboardFragment())
                        .commit()
                }

                clearFields()
            } else {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Login button
        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.loginUser(email, password)
            }
        }

        // Go to signup
        binding.goToSignupBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun saveLoginState(user: User) {
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            putInt("userId", user.userId)
            putBoolean("isAdmin", user.userEmail == "admin@hireherhands.com")
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
