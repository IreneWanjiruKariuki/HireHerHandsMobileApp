package com.example.hhhapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import com.example.hhhapp.database.WorkerSkillCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditWorkerProfileFragment : Fragment() {

    private lateinit var etBio: EditText
    private lateinit var etLocation: EditText
    private lateinit var etHourlyRate: EditText
    private lateinit var etExperience: EditText
    private lateinit var skillsContainer: LinearLayout
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var profileId: Int = -1
    private var currentProfile: WorkerProfile? = null
    private val selectedSkills = mutableSetOf<Int>()

    companion object {
        private const val ARG_PROFILE_ID = "profile_id"

        fun newInstance(profileId: Int): EditWorkerProfileFragment {
            val fragment = EditWorkerProfileFragment()
            val args = Bundle()
            args.putInt(ARG_PROFILE_ID, profileId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = arguments?.getInt(ARG_PROFILE_ID, -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_worker_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etBio = view.findViewById(R.id.etBio)
        etLocation = view.findViewById(R.id.etLocation)
        etHourlyRate = view.findViewById(R.id.etHourlyRate)
        etExperience = view.findViewById(R.id.etExperience)
        skillsContainer = view.findViewById(R.id.skillsContainer)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        loadProfileData()
        loadSkills()

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadProfileData() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val profile = db.WorkerProfileDao().getProfileByWorkerId(profileId)

            if (profile != null) {
                currentProfile = profile

                // Get current skills
                val skillIds = db.WorkerSkillCrossRefDao().getSkillIdsForProfile(profile.profileId)
                selectedSkills.addAll(skillIds)

                withContext(Dispatchers.Main) {
                    etBio.setText(profile.workerBio)
                    etLocation.setText(profile.location)
                    etHourlyRate.setText(profile.hourlyRate.toString())
                    etExperience.setText(profile.experienceYears.toString())
                }
            }
        }
    }

    private fun loadSkills() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val allSkills = db.SkillsDao().getAllSkills()

            withContext(Dispatchers.Main) {
                skillsContainer.removeAllViews()

                for (skill in allSkills) {
                    val checkBox = CheckBox(requireContext())
                    checkBox.text = skill.skillName
                    checkBox.textSize = 16f
                    checkBox.setPadding(8, 8, 8, 8)
                    checkBox.isChecked = selectedSkills.contains(skill.skillId)

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedSkills.add(skill.skillId)
                        } else {
                            selectedSkills.remove(skill.skillId)
                        }
                    }

                    skillsContainer.addView(checkBox)
                }
            }
        }
    }

    private fun saveProfile() {
        val bio = etBio.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val rateStr = etHourlyRate.text.toString().trim()
        val expStr = etExperience.text.toString().trim()

        // Validation
        if (bio.isEmpty() || location.isEmpty() || rateStr.isEmpty() || expStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val hourlyRate = rateStr.toDoubleOrNull()
        val experience = expStr.toIntOrNull()

        if (hourlyRate == null || hourlyRate < 0) {
            Toast.makeText(requireContext(), "Please enter a valid hourly rate", Toast.LENGTH_SHORT).show()
            return
        }

        if (experience == null || experience < 0) {
            Toast.makeText(requireContext(), "Please enter valid years of experience", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSkills.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one skill", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Error: Profile not found", Toast.LENGTH_SHORT).show()
            return
        }

        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update profile
                val updatedProfile = currentProfile!!.copy(
                    workerBio = bio,
                    location = location,
                    hourlyRate = hourlyRate,
                    experienceYears = experience
                )

                db.WorkerProfileDao().updateProfile(updatedProfile)

                // Update skills - delete old ones and insert new ones
                db.WorkerSkillCrossRefDao().deleteByProfileId(currentProfile!!.profileId)

                val skillRefs = selectedSkills.map { skillId ->
                    WorkerSkillCrossRef(currentProfile!!.profileId, skillId)
                }
                db.WorkerSkillCrossRefDao().insertAll(skillRefs)

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