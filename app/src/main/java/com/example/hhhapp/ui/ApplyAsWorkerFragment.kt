package com.example.hhhapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.databinding.FragmentApplyAsWorkerBinding

class ApplyAsWorkerFragment : Fragment() {

    private var _binding: FragmentApplyAsWorkerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApplyAsWorkerViewModel by viewModels()
    private val PICK_IMAGE_REQUEST = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplyAsWorkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = HireHerHandsDatabase.getDatabase(requireContext())

        // ID Picture Selection
        binding.btnSelectId.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                // Request persistable permission
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // skills list for RecyclerView
        val skills = listOf(
            1 to "Plumbing",
            2 to "Cleaning",
            3 to "Electrician",
            4 to "Hairdressing",
            5 to "Painting"
        )

        val selectedSkills = mutableSetOf<Int>()

        // RecyclerView setup
        binding.rvSkills.isNestedScrollingEnabled = false
        binding.rvSkills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSkills.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val cb = CheckBox(parent.context)
                cb.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                return object : RecyclerView.ViewHolder(cb) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val skill = skills[position]
                val cb = holder.itemView as CheckBox
                cb.text = skill.second
                cb.isChecked = selectedSkills.contains(skill.first)

                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedSkills.add(skill.first)
                    else selectedSkills.remove(skill.first)
                }
            }

            override fun getItemCount(): Int = skills.size
        }

        // submit Application
        binding.btnSubmitWorker.setOnClickListener {
            val bio = binding.etWorkerBio.text.toString().trim()
            val hourlyRate = binding.etHourlyRate.text.toString().toDoubleOrNull() ?: 0.0
            val experience = binding.etExperience.text.toString().toIntOrNull() ?: 0
            val location = binding.etLocation.text.toString().trim()

            val sharedPref = requireActivity().getSharedPreferences("HireHerHands", 0)
            val userId = sharedPref.getInt("userId", -1)

            if (bio.isEmpty() || hourlyRate <= 0.0 || experience < 0 || location.isEmpty() || viewModel.idPictureUri == null) {
                Toast.makeText(requireContext(), "Please fill all fields and select ID picture", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Convert the selected URI to a string here
            val uriString = viewModel.idPictureUri.toString()

            //submit to ViewModel
            viewModel.submitApplication(
                db,
                userId,
                bio,
                hourlyRate,
                experience,
                location,
                selectedSkills.toList(),
                idPictureUri = uriString
            )
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            if (selectedImage != null) {
                // CRITICAL FIX: Take persistent URI permission
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        selectedImage,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    // If persistable permission fails, image might still work temporarily
                    Toast.makeText(requireContext(), "Note: Image may not persist after restart", Toast.LENGTH_SHORT).show()
                }

                binding.ivIdPicture.setImageURI(selectedImage)
                viewModel.idPictureUri = selectedImage
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
