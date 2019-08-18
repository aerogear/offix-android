package org.aerogear.graphqlandroid.adapter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.alertdialog_task.view.*
import kotlinx.android.synthetic.main.item_row_tasks.view.*
import okhttp3.internal.Version
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.activities.MainActivity
import org.aerogear.graphqlandroid.fragments.Fragment_Tasks
import org.aerogear.graphqlandroid.model.Task

class TaskAdapter(private val notes: List<Task>, private val context: Context) :
    RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(container.context).inflate(
                R.layout.item_row_tasks,
                container,
                false
            )
        )
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {

        val currentTask = notes[position]
        with(holder.itemView) {
            title_tv.text = "Title: ${currentTask.title}"
            desc_tv.text = "Description: ${currentTask.desc}"
            id_tv.text = "TaskID: ${currentTask.id.toString()}"
            version_tv.text = "Version: ${currentTask.version.toString()}"
        }

        holder.itemView.setOnClickListener {
            val inflatedView = LayoutInflater.from(context).inflate(R.layout.alertdialog_task, null, false)
            val customAlert: AlertDialog = AlertDialog.Builder(context)
                .setView(inflatedView)
                .setTitle("Update the clicked Note")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->

                    val id = inflatedView.etId.toString()
                    val titleEt = inflatedView.etTitleTask.text.toString()
                    val versionEt = inflatedView.etVersion.text.toString()

                    Fragment_Tasks().updateTask(
                        currentTask.id.toString(),
                        titleEt,
                        versionEt.toInt(),
                        "description"
                    )
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }
    }
}
