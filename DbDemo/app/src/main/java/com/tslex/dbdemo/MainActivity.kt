package com.tslex.dbdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = this::class.java.canonicalName

    private lateinit var personRepository: PersonRepository
    private lateinit var adapter: RecyclerView.Adapter<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate")

        personRepository = PersonRepository(this).open()

        recycledView.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        adapter = DataRecyclerViewAdapter(this, personRepository)
        recycledView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")

        personRepository.close()
    }

    fun buttonAddToDb(view: View){

        val person = Person(
            editTextFirstName.text.toString(),
            editTextLastName.text.toString()
        )

        personRepository.add(person)
        refreshData()
    }

    fun buttonGetData(view: View){

        Log.d(TAG, "prepareToGetAll")

        var persons = personRepository.getAll()

        Log.d(TAG, "gotAll!")

        var temp = ""
        for (person in persons){
            temp += "${person.id} - ${person.firstName} ${person.lastNamae}\n"
        }

//        textViewData.text = temp
//        Log.d(TAG, temp)
        refreshData()
    }

    fun buttonEraseData(view: View){
        personRepository.erase()
        refreshData()
    }

    fun refreshData(){
        (adapter as DataRecyclerViewAdapter).refreshData()
        adapter.notifyDataSetChanged()
    }
}
