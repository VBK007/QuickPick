package com.example.quickpick

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.quickpick.Model.QuickpickdataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.ValueEventRegistration
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import java.util.*

class loginactivity : AppCompatActivity() {
    private var linearbottom: LinearLayout? = null
    private var lineartop: LinearLayout? = null
    private  lateinit var regibutton: Button
    private lateinit var loginbutton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_loginactivity)
        linearbottom = findViewById(R.id.botomlinear)
        lineartop = findViewById(R.id.tolinear)
        var topanimation: Animation = AnimationUtils.loadAnimation(this, R.anim.newanim)
        var bottomanm: Animation = AnimationUtils.loadAnimation(this, R.anim.newanim2)

        regibutton = findViewById(R.id.register)
        loginbutton=findViewById(R.id.loginbutton)

regibutton.setOnClickListener(listener)
loginbutton.setOnClickListener(listener)

    }

    private val listener= View.OnClickListener { view ->
        when (view.id) {
            R.id.register -> {
               startActivity(Intent(this,register::class.java))
                finish()
            }

            R.id.loginbutton->{
                //Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
               makeuserlogin()
            }

        }
    }

    private fun makeuserlogin() {

val editemail=findViewById<EditText>(R.id.emailuser)
        val editpass=findViewById<EditText>(R.id.emailpassword)

        val database=FirebaseDatabase.getInstance()
       val auth=FirebaseAuth.getInstance()
        val alertDialog=SpotsDialog.Builder()
            .setMessage("Please Wait")
            .setCancelable(false)
            .setContext(this)
            .build()
        alertDialog.show()

      if (TextUtils.isDigitsOnly(editemail.text.toString())||TextUtils.isDigitsOnly(editpass.text.toString()))
      {
          alertDialog.dismiss()
          Toast.makeText(this,"Please fill all the form",Toast.LENGTH_SHORT).show()

      }
        else{
          auth.signInWithEmailAndPassword(editemail.text.toString(),editpass.text.toString())
              .addOnSuccessListener {
                  val reference=database.reference.child(Commmon.DRIVER_INFO_REFERENCE).child(FirebaseAuth
                      .getInstance().currentUser?.uid.toString()
                  )

                  reference.addListenerForSingleValueEvent(object :ValueEventListener{
                      override fun onCancelled(error: DatabaseError) {

                      }

                      override fun onDataChange(snapshot: DataSnapshot) {
                          alertDialog.dismiss()
                          val intent=Intent(this@loginactivity,mainactivity::class.java)
                          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                          Commmon.currentuser=snapshot.getValue(QuickpickdataModel::class.java)
                          startActivity(intent)
                          finish()
                          Toast.makeText(this@loginactivity,"Login Sucess",Toast.LENGTH_SHORT).show()
                      }


                  })



              }.addOnFailureListener{
                  alertDialog.dismiss()
                  Toast.makeText(this," "+it.message,Toast.LENGTH_SHORT).show()
              }
      }




    }


}