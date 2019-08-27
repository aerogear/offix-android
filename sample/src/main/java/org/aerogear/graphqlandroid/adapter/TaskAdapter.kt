package org.aerogear.graphqlandroid.adapter

import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.alert_update_task.view.*
import kotlinx.android.synthetic.main.alert_update_user.view.*
import kotlinx.android.synthetic.main.alertfrag_create_user.view.*
import kotlinx.android.synthetic.main.alertfrag_create_user.view.etTitleUser
import kotlinx.android.synthetic.main.item_row_tasks.view.*
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.activities.MainActivity
import org.aerogear.graphqlandroid.model.UserOutput


class TaskAdapter(private val notes: List<UserOutput>, private val context: Context) :
    RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPreferences.edit()

    inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): TaskHolder {
        return TaskHolder(
            LayoutInflater.from(container.context).inflate(
                org.aerogear.graphqlandroid.R.layout.item_row_tasks,
                container,
                false
            )
        )
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {

        val currentTask = notes[position]
        var title_task = currentTask.title
        var desc_task = currentTask.desc
        var id_task = currentTask.id.toString()

        with(holder.itemView) {
            title_tv.text = currentTask.title
            desc_tv.text = currentTask.desc
            id_tv.text = currentTask.id.toString()
            if (currentTask.firstName.isNotEmpty()) {
                firstName_tv.text = "${currentTask.firstName} ${currentTask.lastName}"
            } else {
                firstName_tv.text = "User not assigned!"
            }

//            if (sharedPreferences.contains(currentTask.id.toString())) {
//                val bool = sharedPreferences.getBoolean(currentTask.id.toString(), true)
//                this.user_switch.isChecked=bool
//            }

//            if (currentTask.firstName.isNotEmpty()) {
//            Log.e("Adapter $position", user_switch.isChecked.toString())
//            this.user_switch.isChecked=true
//        }
//            if (firstName_tv.text.toString().equals("User not assigned!")) {
//                Log.e("Adapter $position", user_switch.isChecked.toString())
//                user_switch.isChecked=false
//            }
        }

        holder.itemView.setOnClickListener {
            //Used for updating details of user
            val inflatedView = LayoutInflater.from(context)
                .inflate(org.aerogear.graphqlandroid.R.layout.alert_update_task, null, false)
            inflatedView.etId.setText(id_task, TextView.BufferType.EDITABLE)
            inflatedView.etTitle.setText(title_task, TextView.BufferType.EDITABLE)
            inflatedView.etDesc.setText(desc_task, TextView.BufferType.EDITABLE)
            val customAlert: AlertDialog = AlertDialog.Builder(context)
                .setView(inflatedView)
                .setTitle("Update the details of the Task")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    val id = inflatedView.etId.text.toString()
                    val titleEt = inflatedView.etTitle.text.toString()
                    val description = inflatedView.etDesc.text.toString()
                    if (context is MainActivity) this.context.updateTask(
                        id,
                        titleEt,
                        description
                    )
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }

        holder.itemView.user_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //Used for assigning user
                val inflatedView =
                    LayoutInflater.from(context)
                        .inflate(org.aerogear.graphqlandroid.R.layout.alertfrag_create_user, null, false)
                inflatedView.etTaskIdUser.setText(id_task, TextView.BufferType.EDITABLE)
                inflatedView.etTitleUser.setText(title_task, TextView.BufferType.EDITABLE)
                val customAlert: AlertDialog = AlertDialog.Builder(context)
                    .setView(inflatedView)
                    .setTitle("Assign the User a task")
                    .setNegativeButton("No") { dialog, which ->
                        buttonView.setChecked(false)
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { dialog, which ->
                        val taskId = inflatedView.etTaskIdUser.text.toString()
                        val title = inflatedView.etTitleUser.text.toString()
                        val firstName = inflatedView.etFirstName.text.toString()
                        val lastName = inflatedView.etLastName.text.toString()
                        val email = inflatedView.etEmail.text.toString()
                        if (context is MainActivity) this.context.createUser(title, firstName, lastName, email,id_task)
                        dialog.dismiss()
                    }
                    .create()
                customAlert.show()
            } else {
            }
            editor.putBoolean(currentTask.id.toString(), isChecked)
            editor.apply()
        }

        holder.itemView.imgUser.setOnClickListener {
            //Used for updating details of user
            val inflatedView =
                LayoutInflater.from(context).inflate(R.layout.alert_update_user, null, false)
            inflatedView.etIdassigned.setText(id_task, TextView.BufferType.EDITABLE)
            inflatedView.etFname.setText(currentTask.firstName, TextView.BufferType.EDITABLE)
            inflatedView.etLname.setText(currentTask.lastName, TextView.BufferType.EDITABLE)
            inflatedView.etLEmailUSer.setText(currentTask.email, TextView.BufferType.EDITABLE)
            inflatedView.etIdUSer.setText(currentTask.userId, TextView.BufferType.EDITABLE)
            inflatedView.etTitleUser.setText(currentTask.title, TextView.BufferType.EDITABLE)

            val customAlert: AlertDialog = AlertDialog.Builder(context)
                .setView(inflatedView)
                .setTitle("Update details of the User")
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    val taskId = inflatedView.etIdassigned.text.toString()
                    val userId = inflatedView.etIdUSer.text.toString()
                    val title = inflatedView.etTitleUser.text.toString()
                    val firstName = inflatedView.etFname.text.toString()
                    val lastName = inflatedView.etLname.text.toString()
                    val email = inflatedView.etLEmailUSer.text.toString()
                    if (context is MainActivity) this.context.updateUser(userId, taskId, title, firstName, lastName, email)
                    dialog.dismiss()
                }
                .create()
            customAlert.show()
        }
    }
}
