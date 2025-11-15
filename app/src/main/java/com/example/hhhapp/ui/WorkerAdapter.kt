package com.example.hhhapp.ui

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.WorkerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkerAdapter(
    private var workers: List<WorkerProfile>,
    private val db: HireHerHandsDatabase,
    private val vm: AdminViewModel
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    fun setData(newList: List<WorkerProfile>) {
        workers = newList
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        Log.d("WorkerAdapter", "onBindViewHolder called for position $position")
        Log.d("WorkerAdapter", "Worker data: ${workers[position]}")
        holder.bind(workers[position], db, vm)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        Log.d("WorkerAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_worker, parent, false)
        Log.d("WorkerAdapter", "View inflated successfully")
        return WorkerViewHolder(view)
    }

    override fun getItemCount() = workers.size

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.tvWorkerName)
        private val bio = itemView.findViewById<TextView>(R.id.tvWorkerBio)
        private val hourlyRate = itemView.findViewById<TextView>(R.id.tvHourlyRate)
        private val experience = itemView.findViewById<TextView>(R.id.tvExperience)
        private val location = itemView.findViewById<TextView>(R.id.tvLocation)
        private val skillsTv = itemView.findViewById<TextView>(R.id.tvSkills)
        private val ivId = itemView.findViewById<ImageView>(R.id.ivIdPicture)
        private val btnApprove = itemView.findViewById<Button>(R.id.btnApprove)
        private val btnReject = itemView.findViewById<Button>(R.id.btnReject)

        fun bind(worker: WorkerProfile, db: HireHerHandsDatabase, vm: AdminViewModel) {
            name.text = "Worker ID: ${worker.workerID}"
            bio.text = worker.workerBio
            hourlyRate.text = "Hourly Rate: ${worker.hourlyRate}"
            experience.text = "Experience: ${worker.experienceYears} yrs"
            location.text = "Location: ${worker.location}"

            worker.idPictureUri?.let {
                Glide.with(itemView).load(Uri.parse(it)).into(ivId)
            }

            // Load skills async - FIXED VERSION
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val skillIds = db.WorkerSkillCrossRefDao().getSkillIdsForProfile(worker.profileId)
                    val skillNames = skillIds.mapNotNull { db.SkillsDao().getSkillById(it)?.skillName }

                    // FIXED: Use withContext instead of launching new CoroutineScope
                    withContext(Dispatchers.Main) {
                        if (skillNames.isNotEmpty()) {
                            skillsTv.text = "Skills: ${skillNames.joinToString(", ")}"
                        } else {
                            skillsTv.text = "Skills: None listed"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        skillsTv.text = "Skills: Error loading"
                    }
                }
            }

            btnApprove.setOnClickListener {
                vm.approveWorker(worker)
            }

            btnReject.setOnClickListener {
                vm.rejectWorker(worker)
            }
        }
    }
}