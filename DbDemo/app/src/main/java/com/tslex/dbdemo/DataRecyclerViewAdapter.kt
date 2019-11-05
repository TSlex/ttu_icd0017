package com.tslex.dbdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_person_row.view.*

class DataRecyclerViewAdapter(context: Context, private val repo: PersonRepository) :
    RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder>() {

    private var dataset: List<Person> = repo.getAll();

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = inflater.inflate(R.layout.recycler_person_row, parent, false)
        return ViewHolder(rowView)
    }

    override fun getItemCount(): Int {
        return dataset.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = dataset.get(position)
        holder.itemView.textViewId.text = person.id.toString()
        holder.itemView.textViewFirstName.text = person.firstName
        holder.itemView.textViewLastName.text = person.lastNamae
    }

    fun refreshData() {
        dataset = repo.getAll()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}