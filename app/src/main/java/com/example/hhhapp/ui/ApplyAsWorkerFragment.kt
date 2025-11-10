package com.example.hhhapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        // Select ID picture
        binding.btnSelectId.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Submit application
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

            viewModel.submitApplication(db, userId, bio, hourlyRate, experience, location)
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            if (selectedImage != null) {
                binding.ivIdPicture.setImageURI(selectedImage)
                viewModel.idPictureUri = selectedImage
            }
        }
        //back button to go back to dashboard
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

