package com.example.todolist.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.todolist.TodoApplication
import com.example.todolist.databinding.ActivityTaskDetailBinding
import com.example.todolist.model.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    private lateinit var binding: ActivityTaskDetailBinding
    private var currentTask: Task? = null
    private var selectedDeadline: Long? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()
        loadTask()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnClearDeadline.setOnClickListener {
            selectedDeadline = null
            updateDeadlineDisplay()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadTask() {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)

        if (taskId == -1L) {
            // New task mode
            binding.toolbar.title = "New Task"
            binding.btnDelete.visibility = View.GONE
        } else {
            // Edit mode
            binding.toolbar.title = "Edit Task"
            binding.btnDelete.visibility = View.VISIBLE

            val app = application as TodoApplication
            lifecycleScope.launch {
                currentTask = app.taskRepository.getTaskById(taskId)
                currentTask?.let { task ->
                    binding.etTitle.setText(task.title)
                    binding.etDescription.setText(task.description)
                    binding.switchCompleted.isChecked = task.isCompleted

                    task.deadline?.let { deadline ->
                        selectedDeadline = deadline
                        calendar.timeInMillis = deadline
                        updateDeadlineDisplay()
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDeadline = calendar.timeInMillis
                updateDeadlineDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedDeadline = calendar.timeInMillis
                updateDeadlineDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDeadlineDisplay() {
        if (selectedDeadline != null) {
            binding.tvSelectedDeadline.text = dateFormat.format(Date(selectedDeadline!!))
            binding.btnClearDeadline.visibility = View.VISIBLE
        } else {
            binding.tvSelectedDeadline.text = "No deadline set"
            binding.btnClearDeadline.visibility = View.GONE
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val isCompleted = binding.switchCompleted.isChecked

        if (title.isEmpty()) {
            binding.tilTitle.error = "Title is required"
            return
        }

        binding.tilTitle.error = null

        val app = application as TodoApplication

        lifecycleScope.launch {
            try {
                if (currentTask != null) {
                    // Update existing task
                    val updatedTask = currentTask!!.copy(
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        deadline = selectedDeadline,
                        updatedAt = System.currentTimeMillis(),
                        isSynced = false
                    )
                    app.taskRepository.updateTask(updatedTask)
                    Toast.makeText(this@TaskDetailActivity, "Task updated", Toast.LENGTH_SHORT).show()
                } else {
                    // Create new task
                    val user = app.userRepository.getLoggedInUser()
                    val newTask = Task(
                        title = title,
                        description = description,
                        isCompleted = isCompleted,
                        deadline = selectedDeadline,
                        userId = user?.id ?: 1L
                    )
                    app.taskRepository.insertTask(newTask)
                    Toast.makeText(this@TaskDetailActivity, "Task created", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask() {
        currentTask?.let { task ->
            val app = application as TodoApplication
            lifecycleScope.launch {
                try {
                    app.taskRepository.deleteTask(task)
                    Toast.makeText(this@TaskDetailActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@TaskDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

