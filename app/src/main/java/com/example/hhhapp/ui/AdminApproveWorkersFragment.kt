package com.example.hhhapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.databinding.FragmentAdminApproveWorkersBinding

class AdminApproveWorkersFragment : Fragment() {

    private var _binding: FragmentAdminApproveWorkersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: WorkerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminApproveWorkersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = HireHerHandsDatabase.getDatabase(requireContext())
        adapter = WorkerAdapter(emptyList(), db, viewModel)

        binding.recyclerPending.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPending.adapter = adapter

        // DEBUG: Log when observer is set up
        Log.d("AdminApprove", "Setting up observer for pending workers")

        viewModel.pendingWorkers.observe(viewLifecycleOwner) { workers ->
            // DEBUG: Log the data received
            Log.d("AdminApprove", "Received ${workers?.size ?: 0} pending workers")
            workers?.forEach { worker ->
                Log.d("AdminApprove", "Worker: ID=${worker.workerID}, Status=${worker.status}")
            }

            if (workers.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "No pending workers found", Toast.LENGTH_SHORT).show()
                Log.d("AdminApprove", "Workers list is empty or null")
            } else {
                adapter.setData(workers)
                Log.d("AdminApprove", "Adapter updated with ${workers.size} workers")
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                Log.d("AdminApprove", "Message: $message")
            }
        }
        // Back button
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
