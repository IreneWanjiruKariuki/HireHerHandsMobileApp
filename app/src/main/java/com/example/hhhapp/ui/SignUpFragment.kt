package com.example.hhhapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import com.example.hhhapp.database.UserDao
import com.example.hhhapp.databinding.FragmentSignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class SignUpFragment : Fragment(/*R.layout.fragment_signup*/) {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    //viewmodel instance
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup? , savedInstanceState: Bundle?):
    // Initialize binding object
            View{
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Setup role spinner
        setupRoleSpinner()

        //Observe signup result from ViewModel
        userViewModel.signupResult.observe(viewLifecycleOwner) { message ->
            //result message
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            //signup successful, go back to login
            if (message == "Account created successfully!") {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }

        //Signup button implementation
        binding.signupBtn.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            //Validate inputs
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(
                    requireContext(),
                    "Email must contain letters, numbers, @ and . (e.g., user123@example.com)",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (role == "Select Role") {
                Toast.makeText(requireContext(), "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //Create user and insert into database
            val newUser = User(
                userId = 0,
                userName = username,
                userRole = role.lowercase(),
                userEmail = email,
                userPassword = password
            )

            //call the vm to signup
            userViewModel.signupUser(newUser)
        }
        // 4. Back to login button implementation
        binding.goToLoginBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRoleSpinner() {
        // Choose between customer and worker - Admin is hardcoded
        val roles = arrayOf("Select Role", "Customer", "Worker")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        val hasLetter = email.any { it.isLetter() }
        val hasNumber = email.any { it.isDigit() }
        val matchesPattern = email.matches(Regex(emailPattern))

        return matchesPattern && hasLetter && hasNumber
    }

    private fun clearFields() {
        binding.username.text.clear()
        binding.email.text.clear()
        binding.password.text.clear()
        binding.spinnerRole.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
