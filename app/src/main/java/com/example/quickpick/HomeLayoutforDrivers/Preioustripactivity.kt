package com.example.quickpick.HomeLayoutforDrivers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quickpick.Commmon
import com.example.quickpick.R
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Preioustripactivity : AppCompatActivity() {

    lateinit var previousrecyler: RecyclerView
    lateinit var postRecylePrevioustriprViewAdapter: PostRecylePrevioustriprViewAdapter
    var arraylist: ArrayList<Previoustripdatamodel>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.previoustriplayout)
        previousrecyler = findViewById(R.id.previousrecyler)
        arraylist = ArrayList()
        val layoutmanager = GridLayoutManager(this, 2)
        previousrecyler.layoutManager = layoutmanager
        previousrecyler.setHasFixedSize(true)

        postRecylePrevioustriprViewAdapter = PostRecylePrevioustriprViewAdapter(this, arraylist!!)
        previousrecyler.adapter = postRecylePrevioustriprViewAdapter

        val reference = FirebaseDatabase.getInstance();
        reference.getReference(Commmon.DRIVER_PREVIOUS_TRIP)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   arraylist?.clear()
                    for (items in snapshot.children){
                        val model=items.getValue(Previoustripdatamodel::class.java)
                        arraylist?.add(model!!)

                    }

                    postRecylePrevioustriprViewAdapter.addall(arraylist!!)
                    postRecylePrevioustriprViewAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                  Log.d("error","while adding data")
                }

            })


    }


    inner class PostRecylePrevioustriprViewAdapter(
        var context: Context,
        var postListMap: ArrayList<Previoustripdatamodel>
    ) :
        RecyclerView.Adapter<PostRecylePrevioustriprViewAdapter.Previoustrip>() {


        inner class Previoustrip(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val bb: ImageView = itemView.findViewById(R.id.bb)
            val source = itemView.findViewById<TextView>(R.id.source)
            val date: TextView = itemView.findViewById(R.id.date)
            fun bind(position: Int) {
                val cl = postListMap[position]
                Glide.with(context).load(cl.image).into(bb)
                source.text = "From ${cl.source} to ${cl.dest}"
                date.text = "${cl.traveldate}"

                itemView.setOnClickListener {
                    val intent =Intent(context,Describeprofileactivity::class.java)
                    Commmon.previousdatadescribe = cl
                    context.startActivity(intent)

                }

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Previoustrip {
            when (viewType) {
                1 -> {
                    val holder = Previoustrip(
                        LayoutInflater.from(context)
                            .inflate(R.layout.layoutforpreviousfiles, parent, false)
                    )

                    holder.setIsRecyclable(false);
                    return holder

                }
                else -> {
                    val holder = Previoustrip(
                        LayoutInflater.from(context)
                            .inflate(R.layout.layoutforpreviousfiles, parent, false)
                    )

                    holder.setIsRecyclable(false);
                    return holder
                }
            }
        }

        override fun onBindViewHolder(holder: Previoustrip, position: Int) {
            (holder as Previoustrip).bind(position)
        }

        override fun getItemCount(): Int {
            return postListMap.size
        }


        fun addall(r: ArrayList<Previoustripdatamodel>) {
            arraylist?.addAll(r)
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return postListMap[position].viptype
        }


    }


}