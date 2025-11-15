package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditUserProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var userId: Int = -1
    private var currentUser: User? = null

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: Int): EditUserProfileFragment {
            val fragment = EditUserProfileFragment()
            val args = Bundle()
            args.putInt(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getInt(ARG_USER_ID, -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        rgGender = view.findViewById(R.id.rgGender)
        rbMale = view.findViewById(R.id.rbMale)
        rbFemale = view.findViewById(R.id.rbFemale)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        loadUserData()

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadUserData() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val user = db.UserDao().getUserById(userId)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    currentUser = user
                    etName.setText(user.userName)
                    etEmail.setText(user.userEmail)
                    // Don't pre-fill password for security

                    if (user.userGender == "Male") {
                        rbMale.isChecked = true
                    } else {
                        rbFemale.isChecked = true
                    }
                }
            }
        }
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        val selectedGenderId = rgGender.checkedRadioButtonId
        val gender = if (selectedGenderId == R.id.rbMale) "Male" else "Female"

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        // Password validation (only if user wants to change it)
        val finalPassword = if (password.isNotEmpty()) {
            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }
            password
        } else {
            // Keep the old password if no new password is entered
            currentUser?.userPassword ?: ""
        }

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Error: User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if email is already used by another user
                if (email != currentUser!!.userEmail) {
                    val existingUser = db.UserDao().checkEmailExists(email)
                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                // Update user
                val updatedUser = currentUser!!.copy(
                    userName = name,
                    userEmail = email,
                    userPassword = finalPassword,
                    userGender = gender
                )

                db.UserDao().updateUser(updatedUser)

                // Update SharedPreferences if needed
                val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("userEmail", email)
                    apply()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}