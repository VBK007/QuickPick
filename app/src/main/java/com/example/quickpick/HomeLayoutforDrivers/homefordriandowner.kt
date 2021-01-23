package com.example.quickpick.HomeLayoutforDrivers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import com.example.quickpick.Commmon
import com.example.quickpick.EndUserLayouts.Fragment.homefragenduser
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.R
import com.example.quickpick.loginactivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text
import java.lang.StringBuilder

class homefordriandowner : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homefordriandowner)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawer=findViewById<DrawerLayout>(R.id.drawer)
        val toolbar=findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val drawerToggle = ActionBarDrawerToggle(this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        loaduserInfo()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)


        if (savedInstanceState==null){
            val fragent= homefragenduser()
            supportFragmentManager.beginTransaction().replace(R.id.frame,fragent,fragent.javaClass.simpleName).commit()
        }







    }
    private fun loaduserInfo() {
        var name: String? = null
        val reference= FirebaseDatabase.getInstance();
        reference.getReference(Commmon.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    val  model=snapshot.getValue(QuickpickdataModel::class.java)
                    if (FirebaseAuth.getInstance().currentUser?.uid.equals(model?.uid)){
                        name=model?.username
                        val navigationView =
                            findViewById<View>(R.id.nav_view) as NavigationView
                        val headerView = navigationView.getHeaderView(0)
                        val username = headerView.findViewById<TextView>(R.id.username)
                        username.text = model?.username

                        val email=headerView.findViewById<TextView>(R.id.email)
                        email.text = model?.email
                        val phone=headerView.findViewById<TextView>(R.id.phone)
                        phone.text =model?.phonenumber

                        val rating=headerView.findViewById<RelativeLayout>(R.id.invisible)
                        rating.visibility=View.VISIBLE
                        val rr=findViewById<TextView>(R.id.rr)
                        rr.text= StringBuilder(model?.rating.toString())

                    }


                }


            })



    }

    fun upprofile(item: MenuItem) {
        if (item.itemId==R.id.endprofupdate){
            Toast.makeText(this, "Click on setting", Toast.LENGTH_SHORT).show()

        }
    }

    fun prevtravel(item: MenuItem) {
        if (item.itemId==R.id.prevtravel){
            Toast.makeText(this, "Click on setting", Toast.LENGTH_SHORT).show()

        }
    }

    fun bookmytravel(item: MenuItem) {
        if (item.itemId==R.id.booktrave){
            Toast.makeText(this, "Click on setting", Toast.LENGTH_SHORT).show()

        }
    }

    fun logouti(item: MenuItem) {
        if (item.itemId==R.id.logout) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Logout")
            alertDialog.setNegativeButton("Cancel") { dialogInterface,_->dialogInterface.dismiss()
            }

            alertDialog.setPositiveButton("Sigout") { dialogInterface,_->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, loginactivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Logout Succesfully", Toast.LENGTH_SHORT).show()
            }.setCancelable(false)

            val dialog=alertDialog.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(resources.getColor(R.color.black))

            }

            dialog.show()

        }
    }
}