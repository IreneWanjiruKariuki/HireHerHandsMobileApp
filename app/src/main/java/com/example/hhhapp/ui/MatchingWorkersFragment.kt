package com.example.hhhapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.Job
import com.example.hhhapp.databinding.FragmentMatchingWorkersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchingWorkersFragment : Fragment() {

    private var _binding: FragmentMatchingWorkersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchingWorkersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingWorkersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val skillId = arguments?.getInt("skillId") ?: -1
        val jobId = arguments?.getInt("jobId") ?: -1
        if (skillId == -1) {
            Toast.makeText(requireContext(), "Error: Missing skill info", Toast.LENGTH_SHORT).show()
            return
        }

        val db = HireHerHandsDatabase.getDatabase(requireContext())
        viewModel.loadMatchingWorkers(db, skillId)

        viewModel.workers.observe(viewLifecycleOwner) { workerList ->
            binding.workersContainer.removeAllViews()

            if (workerList.isEmpty()) {
                val tv = TextView(requireContext())
                tv.text = "No available workers match this skill."
                tv.textSize = 16f
                binding.workersContainer.addView(tv)
            } else {
                for ((profile, user) in workerList) {
                    val card = layoutInflater.inflate(R.layout.item_worker_card, null)

                    // Corrected IDs matching XML
                    card.findViewById<TextView>(R.id.tvWorkerName).text = user.userName
                    card.findViewById<TextView>(R.id.workerBio).text = profile.workerBio
                    card.findViewById<TextView>(R.id.location).text = "Location: ${profile.location}"
                    card.findViewById<TextView>(R.id.HourlyRate).text = "Hourly Rate: ${profile.hourlyRate}"
                    card.findViewById<TextView>(R.id.experienceYears).text = "Experience: ${profile.experienceYears} years"
                    card.findViewById<TextView>(R.id.averageRating).text = "Rating: ${profile.averageRating}"

                    val btnSelect = card.findViewById<Button>(R.id.btnSelectWorker)
                    btnSelect.setOnClickListener {
                        assignWorkerToJob(db, jobId, profile.workerID)
                    }

                    binding.workersContainer.addView(card)
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun assignWorkerToJob(db: HireHerHandsDatabase, jobId: Int, workerId: Int) {
        if (jobId == -1) {
            Toast.makeText(requireContext(), "Error: Job not found", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            db.JobDao().updateJobStatus(jobId, "PENDING_WORKER_APPROVAL", workerId)
        }

        Toast.makeText(requireContext(), "Worker selected successfully!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

