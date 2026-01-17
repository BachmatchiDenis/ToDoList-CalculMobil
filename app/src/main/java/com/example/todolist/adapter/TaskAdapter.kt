package com.example.todolist.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ItemTaskBinding
import com.example.todolist.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskStatusChange: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                tvTaskTitle.text = task.title
                tvTaskDescription.text = task.description
                cbTaskStatus.isChecked = task.isCompleted

                // Strikethrough completed tasks
                if (task.isCompleted) {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTaskTitle.setTextColor(ContextCompat.getColor(root.context, android.R.color.darker_gray))
                } else {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTaskTitle.setTextColor(ContextCompat.getColor(root.context, android.R.color.black))
                }

                // Show deadline if exists
                if (task.deadline != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    tvDeadline.text = dateFormat.format(Date(task.deadline))
                    tvDeadline.visibility = android.view.View.VISIBLE

                    // Highlight if deadline is approaching
                    if (task.deadline < System.currentTimeMillis() && !task.isCompleted) {
                        tvDeadline.setTextColor(ContextCompat.getColor(root.context, android.R.color.holo_red_dark))
                    } else {
                        tvDeadline.setTextColor(ContextCompat.getColor(root.context, android.R.color.darker_gray))
                    }
                } else {
                    tvDeadline.visibility = android.view.View.GONE
                }

                // Show sync status
                ivSyncStatus.setImageResource(
                    if (task.isSynced) R.drawable.ic_synced else R.drawable.ic_not_synced
                )

                // Click listeners
                root.setOnClickListener { onTaskClick(task) }

                cbTaskStatus.setOnClickListener {
                    onTaskStatusChange(task)
                }

                btnDelete.setOnClickListener {
                    onTaskDelete(task)
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}

