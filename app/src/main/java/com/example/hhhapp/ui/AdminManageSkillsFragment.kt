package com.example.hhhapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hhhapp.R
import com.example.hhhapp.database.HireHerHandsDatabase
import com.example.hhhapp.database.Skills
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminManageSkillsFragment : Fragment() {

    private lateinit var skillsContainer: LinearLayout
    private lateinit var btnAddSkill: Button
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_manage_skills, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skillsContainer = view.findViewById(R.id.skillsContainer)
        btnAddSkill = view.findViewById(R.id.btnAddSkill)
        btnBack = view.findViewById(R.id.btnBack)

        btnAddSkill.setOnClickListener {
            showAddSkillDialog()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadSkills()
    }

    private fun loadSkills() {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val skills = db.SkillsDao().getAllSkills()

            withContext(Dispatchers.Main) {
                skillsContainer.removeAllViews()

                if (skills.isEmpty()) {
                    val tv = TextView(requireContext())
                    tv.text = "No skills found"
                    tv.textSize = 16f
                    tv.setPadding(16, 16, 16, 16)
                    skillsContainer.addView(tv)
                } else {
                    for (skill in skills) {
                        displaySkillCard(skill, db)
                    }
                }
            }
        }
    }

    private fun displaySkillCard(skill: Skills, db: HireHerHandsDatabase) {
        val card = layoutInflater.inflate(R.layout.item_skill_card, skillsContainer, false)

        card.findViewById<TextView>(R.id.tvSkillName).text = skill.skillName
        card.findViewById<TextView>(R.id.tvSkillId).text = "ID: ${skill.skillId}"

        val btnDelete = card.findViewById<Button>(R.id.btnDeleteSkill)
        btnDelete.setOnClickListener {
            showDeleteConfirmation(skill, db)
        }

        // Show worker count for this skill
        CoroutineScope(Dispatchers.IO).launch {
            val workers = db.WorkerProfileDao().getApprovedWorkersBySkill(skill.skillId)
            withContext(Dispatchers.Main) {
                card.findViewById<TextView>(R.id.tvWorkerCount).text = "Workers: ${workers.size}"
            }
        }

        skillsContainer.addView(card)
    }

    private fun showAddSkillDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Skill")

        val input = EditText(requireContext())
        input.hint = "Enter skill name"
        input.setPadding(50, 20, 50, 20)
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val skillName = input.text.toString().trim()
            if (skillName.isNotEmpty()) {
                addSkill(skillName)
            } else {
                Toast.makeText(requireContext(), "Skill name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addSkill(skillName: String) {
        val db = HireHerHandsDatabase.getDatabase(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the highest skill ID and add 1
                val allSkills = db.SkillsDao().getAllSkills()
                val newSkillId = if (allSkills.isEmpty()) 1 else (allSkills.maxOf { it.skillId } + 1)

                val newSkill = Skills(skillId = newSkillId, skillName = skillName)
                db.SkillsDao().insertSkill(newSkill)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Skill added successfully", Toast.LENGTH_SHORT).show()
                    loadSkills()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error adding skill: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation(skill: Skills, db: HireHerHandsDatabase) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Skill")
        builder.setMessage("Are you sure you want to delete '${skill.skillName}'? This will remove it from all worker profiles.")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteSkill(skill, db)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun deleteSkill(skill: Skills, db: HireHerHandsDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use DAO methods instead of raw SQL
                db.WorkerSkillCrossRefDao().deleteBySkillId(skill.skillId)
                db.SkillsDao().deleteSkill(skill)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Skill deleted successfully", Toast.LENGTH_SHORT).show()
                    loadSkills()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error deleting skill: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
