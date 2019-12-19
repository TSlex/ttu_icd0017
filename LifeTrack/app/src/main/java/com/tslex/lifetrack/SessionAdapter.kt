package com.tslex.lifetrack

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tslex.lifetrack.repo.SessionRepo
import kotlinx.android.synthetic.main.sessions_list_item.view.*
import java.text.SimpleDateFormat

class SessionAdapter(private val context: Context, private val repo: SessionRepo) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    private var dataSet = repo.getAll()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var recycler: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = recyclerView
        dataSet.reverse()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.sessions_list_item, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return dataSet.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = dataSet[position]
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val context = context as SessionListUi
        holder.itemView.sessionId.text = session.id.toString()
        holder.itemView.sessionTime.text = sdf.format(session.creatingTime)

        holder.itemView.buttonDelete.setOnClickListener {
            context.delete(session.id)
            repo.open()
            dataSet = repo.getAll()
            dataSet.reverse()
            repo.close()
            context.update()
        }

        holder.itemView.buttonExport.setOnClickListener {
            context.export(session.id)
        }

        holder.itemView.buttonView.setOnClickListener {
            context.view(session.id)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}