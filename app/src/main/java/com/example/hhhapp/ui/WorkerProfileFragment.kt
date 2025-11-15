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
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkerProfileFragment : Fragment() {

    private lateinit var tvBio: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvHourlyRate: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSkills: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button

    private var currentProfile: WorkerProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvBio = view.findViewById(R.id.tvBio)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvHourlyRate = view.findViewById(R.id.tvHourlyRate)
        tvExperience = view.findViewById(R.id.tvExperience)
        tvRating = view.findViewById(R.id.tvRating)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvSkills = view.findViewById(R.id.tvSkills)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnDelete = view.findViewById(R.id.btnDelete)
        btnBack = view.findViewById(R.id.btnBack)

        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val workerId = sharedPref.getInt("userId", -1)

        loadProfile(workerId)

        btnEdit.setOnClickListener {
            if (currentProfile != null) {
                val fragment = EditWorkerProfileFragment.newInstance(currentProfile!!.profileId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "No profile to edit", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            deleteProfile()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadProfile(workerId: Int) {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val profile = db.WorkerProfileDao().getProfileByWorkerId(workerId)

            if (profile != null) {
                // Get skills for this profile
                val skillIds = db.WorkerSkillCrossRefDao().getSkillIdsForProfile(profile.profileId)
                val skills = mutableListOf<String>()
                for (skillId in skillIds) {
                    val skill = db.SkillsDao().getSkillById(skillId)
                    if (skill != null) {
                        skills.add(skill.skillName)
                    }
                }

                withContext(Dispatchers.Main) {
                    currentProfile = profile
                    tvBio.text = "Bio: ${profile.workerBio}"
                    tvLocation.text = "Location: ${profile.location}"
                    tvHourlyRate.text = "Hourly Rate: KSh ${profile.hourlyRate}"
                    tvExperience.text = "Experience: ${profile.experienceYears} years"
                    tvRating.text = "Rating: ${profile.averageRating}/5.0"
                    tvStatus.text = "Status: ${profile.status}"

                    if (skills.isNotEmpty()) {
                        tvSkills.text = "Skills: ${skills.joinToString(", ")}"
                    } else {
                        tvSkills.text = "Skills: None"
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    tvBio.text = "No profile found. Please create a worker profile first."
                    tvLocation.text = ""
                    tvHourlyRate.text = ""
                    tvExperience.text = ""
                    tvRating.text = ""
                    tvStatus.text = ""
                    tvSkills.text = ""
                    btnEdit.isEnabled = false
                    btnDelete.isEnabled = false
                }
            }
        }
    }

    private fun deleteProfile() {
        if (currentProfile == null) return

        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
            val workerId = sharedPref.getInt("userId", -1)

            // Delete worker-skill relationships first
            db.WorkerSkillCrossRefDao().deleteByProfileId(currentProfile!!.profileId)

            // Then delete profile
            db.WorkerProfileDao().deleteAllProfilesForWorker(workerId)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit
        val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
        val workerId = sharedPref.getInt("userId", -1)
        loadProfile(workerId)
    }
}