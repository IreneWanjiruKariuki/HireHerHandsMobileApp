package com.example.hhhapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.database.Job
import com.example.hhhapp.databinding.FragmentPostJobBinding
import java.text.SimpleDateFormat
import java.util.*

class PostJobFragment : Fragment() {

    private var _binding: FragmentPostJobBinding? = null
    private val binding get() = _binding!!

    private val jobViewModel: JobViewModel by viewModels()

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

        //setup skill dropdown (temporary static list for now)
        val skills = arrayOf("Select Skill", "Plumbing", "Cleaning", "Electrician", "Hairdressing")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, skills)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSkill.adapter = adapter

        //observe messages from ViewModel
        jobViewModel.message.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        //handle Post Job button
        binding.btnPostJob.setOnClickListener {
            val title = binding.etJobTitle.text.toString().trim()
            val description = binding.etJobDescription.text.toString().trim()
            val location = binding.etJobLocation.text.toString().trim()
            val date = binding.etJobDate.text.toString().trim()
            val budgetText = binding.etJobBudget.text.toString().trim()
            val skillIndex = binding.spinnerSkill.selectedItemPosition

            //validate input
            if (title.isEmpty() || description.isEmpty() || location.isEmpty() ||
                date.isEmpty() || budgetText.isEmpty() || skillIndex == 0
            ) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetText.toDoubleOrNull()
            if (budget == null) {
                Toast.makeText(requireContext(), "Please enter a valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //get logged-in user (customer)
            val sharedPref = requireActivity().getSharedPreferences("HireHerHands", Context.MODE_PRIVATE)
            val customerId = sharedPref.getInt("userId", -1)

            //convert selected skill index to skillId (example: 1-based index)
            val skillId = skillIndex

            //create Job object
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

            // save job using ViewModel
            jobViewModel.postJob(newJob)

            clearFields()
        }


        //back button to go back to dashboard
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
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
