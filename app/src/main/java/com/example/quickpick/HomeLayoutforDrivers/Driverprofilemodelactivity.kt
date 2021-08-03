package com.example.quickpick.HomeLayoutforDrivers

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.quickpick.Commmon
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.R
import com.google.firebase.database.*

class Driverprofilemodelactivity : AppCompatActivity() {
    lateinit var drivername: TextView
    lateinit var mobilenumberdriver: TextView
    lateinit var emailfordriver: TextView
    lateinit var companyname: TextView
    lateinit var companyaddress: TextView
    lateinit var vehiclenumeber: TextView
    lateinit var rating: TextView
    var email: String = ""
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_profile_activity)
        drivername = findViewById(R.id.tv_hsted_name)
        mobilenumberdriver = findViewById(R.id.tv_mbilnum)
        emailfordriver = findViewById(R.id.hostmailidoptional)
        companyname = findViewById(R.id.tv_fud_avb)
        companyaddress = findViewById(R.id.tv_loc_detail)
        vehiclenumeber = findViewById(R.id.tv_phoneno)
        rating = findViewById(R.id.tv_eve_type)

        email = intent.getStringExtra("email").toString()


        val reference = FirebaseDatabase.getInstance();
        reference.getReference(Commmon.DRIVER_INFO_REFERENCE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   for (postsnapshot in snapshot.children) {
                        val model = postsnapshot.getValue(QuickpickdataModel::class.java)
                        Log.e("modeldata", model?.email.toString())
                        if (model?.email == email) {
                            drivername.text = model?.username
                            mobilenumberdriver.text = model?.phonenumber
                            emailfordriver.text = model?.email
                            companyname.text = model?.companyname
                            companyaddress.text = model?.companyaddress
                            rating.text = model?.rating.toString()
                            break

                        }

                    }




                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("errorwhile retrive", error.toString())
                }

            })


    }


}