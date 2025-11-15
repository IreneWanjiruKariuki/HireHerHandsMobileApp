package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserGender: TextView
    private lateinit var tvUserId: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnBack: Button

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserGender = view.findViewById(R.id.tvUserGender)
        tvUserId = view.findViewById(R.id.tvUserId)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnBack = view.findViewById(R.id.btnBack)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)

        loadUserProfile(userId)

        btnEditProfile.setOnClickListener {
            if (currentUser != null) {
                val fragment = EditUserProfileFragment.newInstance(currentUser!!.userId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadUserProfile(userId: Int) {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val user = db.UserDao().getUserById(userId)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    currentUser = user
                    tvUserName.text = "Name: ${user.userName}"
                    tvUserEmail.text = "Email: ${user.userEmail}"
                    tvUserGender.text = "Gender: ${user.userGender}"
                    tvUserId.text = "User ID: ${user.userId}"
                } else {
                    tvUserName.text = "User not found"
                    btnEditProfile.isEnabled = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", -1)
        loadUserProfile(userId)
    }
}