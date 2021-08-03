package com.example.quickpick.HomeLayoutforDrivers

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.quickpick.Commmon
import com.example.quickpick.R
import com.google.android.gms.common.internal.service.Common
import javax.sql.CommonDataSource

class Describeprofileactivity : AppCompatActivity() {

    lateinit var drivername: TextView
    lateinit var mobilenumberdriver: TextView
    lateinit var emailfordriver: TextView
    lateinit var companyname: TextView
    lateinit var companyaddress: TextView
    lateinit var vehiclenumeber: TextView
    lateinit var rating: TextView
    lateinit var image: ImageView
    lateinit var money: TextView
    lateinit var distance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.describeprevioustrip)


        drivername = findViewById(R.id.tv_hsted_name)
        mobilenumberdriver = findViewById(R.id.tv_mbilnum)
        emailfordriver = findViewById(R.id.hostmailidoptional)
        companyname = findViewById(R.id.tv_fud_avb)
        companyaddress = findViewById(R.id.tv_loc_detail)
        vehiclenumeber = findViewById(R.id.tv_phoneno)
        rating = findViewById(R.id.tv_eve_type)
        image = findViewById(R.id.image)
        distance = findViewById(R.id.tv_addcalndr)
        money = findViewById(R.id.money)



        if (Commmon.previousdatadescribe != null) {
            val cl = Commmon.previousdatadescribe
            Glide.with(this).load(cl?.image).into(image)
            drivername.text = cl?.drivername
            mobilenumberdriver.text = cl?.driphonenumber
            emailfordriver.text = cl?.driemail
            companyname.text = cl?.endusername
            companyaddress.text = cl?.endphonenumber
            vehiclenumeber.text = cl?.Vehiclenumber
            rating.text = "From ${cl?.source} to ${cl?.dest}"
            money.text = "Rs.${cl?.money}"
            distance.text = "${cl?.distance} KM"


        }


    }
}