package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.Job
import com.example.hhhapp.database.Skills
import com.example.hhhapp.databinding.FragmentPostJobBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostJobFragment : Fragment() {

    private var _binding: FragmentPostJobBinding? = null
    private val binding get() = _binding!!

    private val jobViewModel: JobViewModel by viewModels()
    private var skillsList: List<Skills> = emptyList() // actual skills from DB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = HireHerHandsDatabase.getDatabase(requireContext())

        // Load skills from DB
        CoroutineScope(Dispatchers.IO).launch {
            skillsList = db.SkillsDao().getAllSkills()
            val skillNames = listOf("Select Skill") + skillsList.map { it.skillName }
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    skillNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSkill.adapter = adapter
            }
        }

        // Observe job ID once job is posted
        jobViewModel.newJobId.observe(viewLifecycleOwner) { jobId ->
            if (jobId == null || jobId <= 0) return@observe
            if (skillsList.isEmpty()) return@observe // wait until skills loaded

            val selectedPosition = binding.spinnerSkill.selectedItemPosition
            if (selectedPosition == 0) return@observe // "Select Skill"

            val skillId = skillsList[selectedPosition - 1].skillId

            Log.d(
                "PostJobFragment",
                "Navigating to MatchingWorkersFragment | jobId: $jobId, skillId: $skillId"
            )

            val fragment = MatchingWorkersFragment()
            fragment.arguments = Bundle().apply {
                putInt("jobId", jobId.toInt())
                putInt("skillId", skillId)
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()

            jobViewModel.resetJobId() // prevent re-navigation
        }

        // Handle Post Job button click
        binding.btnPostJob.setOnClickListener {
            val title = binding.etJobTitle.text.toString().trim()
            val description = binding.etJobDescription.text.toString().trim()
            val location = binding.etJobLocation.text.toString().trim()
            val date = binding.etJobDate.text.toString().trim()
            val budgetText = binding.etJobBudget.text.toString().trim()
            val selectedPosition = binding.spinnerSkill.selectedItemPosition

            if (title.isEmpty() || description.isEmpty() || location.isEmpty() ||
                date.isEmpty() || budgetText.isEmpty() || selectedPosition == 0
            ) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetText.toDoubleOrNull()
            if (budget == null) {
                Toast.makeText(requireContext(), "Please enter a valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (skillsList.isEmpty()) {
                Toast.makeText(requireContext(), "Skills not loaded yet. Try again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val skillId = skillsList[selectedPosition - 1].skillId
            val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
            val customerId = sharedPref.getInt("userId", -1)

            val newJob = Job(
                jobTitle = title,
                jobDescription = description,
                jobLocation = location,
                jobDate = date,
                jobBudget = budget,
                jobStatus = "PENDING_SELECTION",
                customerId = customerId,
                workerId = null,
                skillId = skillId
            )

            jobViewModel.postJob(newJob)
            clearFields()
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        jobViewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        binding.etJobTitle.text.clear()
        binding.etJobDescription.text.clear()
        binding.etJobLocation.text.clear()
        binding.etJobDate.text.clear()
        binding.etJobBudget.text.clear()
        binding.spinnerSkill.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
