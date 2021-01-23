package com.example.quickpick

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Layout
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.Model.QuickpickdataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log

class register : AppCompatActivity() {
    var chooice = arrayOf<String>("Please choose category", "Owner", "Driver", "EndUser")
    private lateinit var databse: FirebaseDatabase;
    private lateinit var reference: DatabaseReference;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        databse = FirebaseDatabase.getInstance()
        reference = databse.getReference(Commmon.DRIVER_INFO_REFERENCE)


        val owner = findViewById<View>(R.id.own)
        val driver = findViewById<View>(R.id.driv)

        val user = findViewById<View>(R.id.enduse)

        val mainlay = findViewById<View>(R.id.mainlay)
        val spin = findViewById<Spinner>(R.id.spinner)

        val colorValue = ContextCompat.getColor(this, R.color.mycolo1)

        val colorValue1 = ContextCompat.getColor(this, R.color.corlo)
        val colorwhite = ContextCompat.getColor(this, R.color.white)
        val layout = findViewById<View>(R.id.middle)
        val image = findViewById<View>(R.id.main)

        val colorValue2 = ContextCompat.getColor(this, R.color.viole)


        val colorValue3 = ContextCompat.getColor(this, R.color.honeysuckl)


        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            chooice
        )

        ad.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spin.adapter = ad

        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {


                when (position) {
                    0 -> {
                        layout.visibility = View.GONE
                        owner.visibility = View.GONE
                        mainlay.setBackgroundColor(colorValue)
                        driver.visibility = View.GONE
                        user.visibility = View.GONE
                        image.visibility = View.VISIBLE


                    }
                    1 -> {
                        layout.visibility = View.VISIBLE
                        owner.visibility = View.VISIBLE
                        mainlay.setBackgroundColor(colorValue1)
                        driver.visibility = View.GONE
                        user.visibility = View.GONE
                        image.visibility = View.GONE
                        layout.setBackgroundColor(colorwhite)

                        saveownerr()


                    }

                    2 -> {
                        layout.visibility = View.VISIBLE
                        owner.visibility = View.GONE
                        mainlay.setBackgroundColor(colorValue2)
                        driver.visibility = View.VISIBLE
                        user.visibility = View.GONE
                        image.visibility = View.GONE
                        layout.setBackgroundColor(colorwhite)
                        savedriver()
                    }
                    3 -> {
                        layout.visibility = View.VISIBLE
                        owner.visibility = View.GONE
                        mainlay.setBackgroundColor(colorValue3)
                        driver.visibility = View.GONE
                        user.visibility = View.VISIBLE
                        image.visibility = View.GONE
                        layout.setBackgroundColor(colorwhite)
                        saveuswer()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }


        }


    }

    private fun saveuswer() {
        val edtfirstname = findViewById<EditText>(R.id.usernamee)
        val edtpase = findViewById<EditText>(R.id.passworde)

        val edtemail = findViewById<EditText>(R.id.emailusere)
        val edtphone = findViewById<EditText>(R.id.phone_numbere)

        val login = findViewById<Button>(R.id.logine)

        login.setOnClickListener {

            val dialog: android.app.AlertDialog = SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build()

            dialog.show()

            if (TextUtils.isEmpty(edtfirstname.text.toString())
                || TextUtils.isEmpty(edtemail.text.toString())
                || TextUtils.isEmpty(edtphone.text.toString())
                || TextUtils.isEmpty(edtpase.text.toString())
            ) {
                dialog.dismiss()
                Toast.makeText(this, "Please fill all the form", Toast.LENGTH_SHORT).show()
            } else {


                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(
                    edtemail.text.toString(),
                    edtpase.text.toString()
                )
                    .addOnSuccessListener {
                        val firebaseuser = auth.currentUser
                        val userid = firebaseuser?.uid
                        val model = QuickpickdataModel()
                        model.username = edtfirstname.text.toString()
                        model.password = edtpase.text.toString()
                        model.email = edtemail.text.toString()
                        model.phonenumber = edtphone.text.toString()
                        model.uid = userid!!
                        model.isEnduser=true
                        model.registerdate=System.currentTimeMillis().toString()
                        reference.child(userid).setValue(model).addOnSuccessListener {

                            val intent = Intent(this, HomeENdUser::class.java)
                            Commmon.currentuser = model
                            startActivity(intent)
                            Toast.makeText(this, "Register Succesfully", Toast.LENGTH_LONG).show()
                            dialog.dismiss()

                        }.addOnFailureListener { e ->

                            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }


                    }.addOnFailureListener { e ->

                        Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@addOnFailureListener

                    }


            }
        }

    }

    private fun savedriver() {


        val edtfirstname = findViewById<EditText>(R.id.usernamed)
        val edtpase = findViewById<EditText>(R.id.passwordd)

        val edtemail = findViewById<EditText>(R.id.emailuserd)
        val edtphone = findViewById<EditText>(R.id.phone_numberd)

        val edtcompanyname = findViewById<EditText>(R.id.companynamed)

        val edtcompanyaddress = findViewById<EditText>(R.id.addressd)

        val vehiclenumber1 = findViewById<EditText>(R.id.vehiclenumberd)

        val vehicletype1 = findViewById<EditText>(R.id.vehicletype1d)

        val login = findViewById<Button>(R.id.logind)



        login.setOnClickListener {

            val dialog: android.app.AlertDialog = SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build()

            dialog.show()

            if (TextUtils.isEmpty(edtfirstname.text.toString())
                || TextUtils.isEmpty(edtemail.text.toString())
                || TextUtils.isEmpty(edtphone.text.toString())
                || TextUtils.isEmpty(edtpase.text.toString())
                || TextUtils.isEmpty(edtcompanyname.text.toString())
                || TextUtils.isEmpty(edtcompanyaddress.text.toString())
                || TextUtils.isEmpty(vehiclenumber1.text.toString())
                || TextUtils.isEmpty(vehicletype1.text.toString())

            ) {
                dialog.dismiss()
                Toast.makeText(this, "Please fill all the form", Toast.LENGTH_SHORT).show()
            } else {


                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(
                    edtemail.text.toString(),
                    edtpase.text.toString()
                )
                    .addOnSuccessListener {
                        val firebaseuser = auth.currentUser
                        val userid = firebaseuser?.uid
                        val model = QuickpickdataModel()
                        model.username = edtfirstname.text.toString()
                        model.password = edtpase.text.toString()
                        model.email = edtemail.text.toString()
                        model.companyname = edtcompanyname.text.toString().toUpperCase(Locale.ROOT)
                        model.phonenumber = edtphone.text.toString()
                        model.companyaddress =
                            edtcompanyaddress.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle1number =
                            vehiclenumber1.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle1 = vehicletype1.text.toString().toUpperCase(Locale.ROOT)
                        model.uid = userid!!
                        model.demail = edtemail.text.toString()
                        model.isEnduser=false
                        model.registerdate=System.currentTimeMillis().toString()
                        reference.child(userid).setValue(model).addOnSuccessListener {

                            val intent = Intent(this, mainactivity::class.java)
                            Commmon.currentuser = model
                            startActivity(intent)
                            Toast.makeText(this, "Register Succesfully", Toast.LENGTH_LONG).show()
                            dialog.dismiss()

                        }.addOnFailureListener { e ->

                            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }


                    }.addOnFailureListener { e ->

                        Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@addOnFailureListener

                    }


            }
        }


    }

    private fun saveownerr() {

        val edtfirstname = findViewById<EditText>(R.id.username)
        val edtpase = findViewById<EditText>(R.id.password)

        val edtemail = findViewById<EditText>(R.id.emailuser)
        val edtphone = findViewById<EditText>(R.id.phone_number)

        val edtcompanyname = findViewById<EditText>(R.id.companyname)

        val edtcompanyaddress = findViewById<EditText>(R.id.address)

        val vehiclenumber1 = findViewById<EditText>(R.id.vehiclenumber)

        val vehicletype1 = findViewById<EditText>(R.id.vehicletype1)

        val vehiclenumber2 = findViewById<EditText>(R.id.vehiclenumber2)

        val vehicletype2 = findViewById<EditText>(R.id.vehicletype2)

        val login = findViewById<Button>(R.id.login)



        login.setOnClickListener {

            val dialog: android.app.AlertDialog = SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build()

            dialog.show()

            if (TextUtils.isEmpty(edtfirstname.text.toString())
                || TextUtils.isEmpty(edtemail.text.toString())
                || TextUtils.isEmpty(edtphone.text.toString())
                || TextUtils.isEmpty(edtpase.text.toString())
                || TextUtils.isEmpty(edtcompanyname.text.toString())
                || TextUtils.isEmpty(edtcompanyaddress.text.toString())
                || TextUtils.isEmpty(vehiclenumber1.text.toString())
                || TextUtils.isEmpty(vehicletype1.text.toString())
                || TextUtils.isEmpty(vehiclenumber2.text.toString())
                || TextUtils.isEmpty(vehicletype2.text.toString())
            ) {
                dialog.dismiss()
                Toast.makeText(this, "Please fill all the form", Toast.LENGTH_SHORT).show()
            } else {


                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(
                    edtemail.text.toString(),
                    edtpase.text.toString()
                )
                    .addOnSuccessListener {
                        val firebaseuser = auth.currentUser
                        val userid = firebaseuser?.uid
                        val model = QuickpickdataModel()
                        model.username = edtfirstname.text.toString()
                        model.password = edtpase.text.toString()
                        model.email = edtemail.text.toString()
                        model.companyname = edtcompanyname.text.toString().toUpperCase(Locale.ROOT)
                        model.phonenumber = edtphone.text.toString()
                        model.companyaddress =
                            edtcompanyaddress.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle1number =
                            vehiclenumber1.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle1 = vehicletype1.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle2number =
                            vehiclenumber2.text.toString().toUpperCase(Locale.ROOT)
                        model.vehicle2 = vehicletype2.text.toString().toUpperCase(Locale.ROOT)
                        model.uid = userid!!
                        model.oemail = edtemail.text.toString()
                        model.isEnduser=false
                        model.registerdate=System.currentTimeMillis().toString()
                        reference.child(userid).setValue(model).addOnSuccessListener {

                            val intent = Intent(this, mainactivity::class.java)
                            Commmon.currentuser = model
                            startActivity(intent)
                            Toast.makeText(this, "Register Succesfully", Toast.LENGTH_LONG).show()
                            dialog.dismiss()

                        }.addOnFailureListener { e ->

                            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }


                    }.addOnFailureListener { e ->

                        Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@addOnFailureListener

                    }


            }
        }

    }


}

