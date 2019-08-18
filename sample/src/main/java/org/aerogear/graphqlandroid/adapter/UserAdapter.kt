package org.aerogear.graphqlandroid.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_row_users.view.*
import org.aerogear.graphqlandroid.R
import org.aerogear.graphqlandroid.model.User

class UserAdapter(private val users: List<User>, private var context: Context) :
    RecyclerView.Adapter<UserAdapter.UserHolder>() {

    inner class UserHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(container: ViewGroup, p1: Int): UserHolder {
        context = container.context
        return UserHolder(LayoutInflater.from(container.context).inflate(R.layout.item_row_users, container, false))
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val currentUser = users[position]
        with(holder.itemView) {
            first_name_tv.text = "FirstName: ${currentUser.firstName}"
            last_name_tv.text = "LastName: ${currentUser.lastName}"
            email_user_tv.text = "Email: ${currentUser.email}"
            taskIdUser_tv.text = "TaskId: ${currentUser.taskId.toString()}"
            title_user_tv.text = "Title: ${currentUser.title}"
        }
//TODO Give the update user functionality.
    }
}
