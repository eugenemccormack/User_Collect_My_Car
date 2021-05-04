package com.example.user_collect_my_car

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.UserModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register_user.*
import java.util.*

class RegisterUser : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference

    private var fileUri : Uri? = null

    private var licenceFileUri : Uri? = null

    var buttonID = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        iv_profileImage.setOnClickListener {

            selectImage()
        }

        insurance_upload_button_register.setOnClickListener {

            buttonID = true

            selectLicenceDocuments()
        }

        register_button_register.setOnClickListener {

            register()
        }

        not_signed_up.setOnClickListener {

            val intent = Intent(this, LoginUser::class.java)

            startActivity(intent)
        }
    }

    private fun selectLicenceDocuments() {

        ImagePicker.with(this)
            .crop().compress(1024).maxResultSize(1080, 1080).start()
    }

    private fun selectImage() {

        ImagePicker.with(this)
            .crop().compress(1024).maxResultSize(1080, 1080).start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK -> {

                if(buttonID){

                    buttonID = false

                    licenceFileUri = data?.data
                }

                else{

                fileUri = data?.data

                iv_profileImage.setImageURI(fileUri)

                    }
            }

            ImagePicker.RESULT_ERROR -> {

                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()

            }

            else -> {

                Toast.makeText(this, "Image Selection Cancelled", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun register(){

        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        val model = UserModel()

        model.name = username_editText_register.text.toString()
        model.phone = phone_editText_register.text.toString()
        model.email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()
        val confirmPassword = confirm_password_editText_register.text.toString()

        model.address1 = address1_editText_register.text.toString()
        model.address2 = address2_editText_register.text.toString()
        model.county = county_editText_register.text.toString()

        model.carReg = car_reg_editText_register.text.toString()
        model.carMake = carmake_editText_register.text.toString()
        model.carModel = carmodel_editText_register.text.toString()
        model.carColour = car_colour_editText_register.text.toString()

        model.insurer = insurance_name_editText_register.text.toString()
        model.insuranceType = insurance_cover_editText_register.text.toString()
        val insuranceExpiry = insurance_expiry_editText_register.text.toString()

        if(model.name!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Name", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.phone!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Phone Number", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.email!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Email Address", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.address1!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter an Address Line 1", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.address2!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter an Address Line 2", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.county!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a County", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.insurer!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Car Insurer Name", Toast.LENGTH_SHORT).show()

            return
        }

        else if(model.insuranceType!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Car Insurance Cover / Type", Toast.LENGTH_SHORT).show()

            return
        }

        else if(insuranceExpiry.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Car Insurance Expiry Date", Toast.LENGTH_SHORT).show()

            return
        }

        else if(password.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Password", Toast.LENGTH_SHORT).show()

            return
        }

        else if(confirmPassword.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Confirm Password", Toast.LENGTH_SHORT).show()

            return
        }

        if(password != confirmPassword){

            Toast.makeText(this, "ERROR - Confirmation Password Does Not Match, Try Again", Toast.LENGTH_SHORT).show()

            return
        }

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return
        }

        if(licenceFileUri == null) {

            Toast.makeText(this, "ERROR - Please Upload Licence Details", Toast.LENGTH_SHORT).show()

            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(model.email!!, password)
            .addOnCompleteListener{

                if(!it.isSuccessful) return@addOnCompleteListener

                uploadImageToFireBaseStorage()
            }

            .addOnFailureListener{

                Toast.makeText(this, "ERROR - Fail to Create User : ${it.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadImageToFireBaseStorage() {

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return
        }

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(fileUri!!)
            .addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener {

                    saveUserInfo(it.toString())
                }
            }
    }


    private fun saveUserInfo(profileImageUrl: String){

        val filename = UUID.randomUUID().toString()

        val ref2 = FirebaseStorage.getInstance().getReference("/insurancedetails/$filename")

        ref2.putFile(licenceFileUri!!)
                .addOnSuccessListener {

                    ref2.downloadUrl.addOnSuccessListener {

                        var photos = it.toString()

                        val model = UserModel()

                        database = FirebaseDatabase.getInstance()
                        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

                        model.image = profileImageUrl
                        model.name = username_editText_register.text.toString()
                        model.phone = phone_editText_register.text.toString()
                        model.email = email_editText_register.text.toString()
                        model.address1 = address1_editText_register.text.toString()
                        model.address2 = address2_editText_register.text.toString()
                        model.county = county_editText_register.text.toString()

                        model.carReg = car_reg_editText_register.text.toString()
                        model.carMake = carmake_editText_register.text.toString()
                        model.carModel = carmodel_editText_register.text.toString()
                        model.carColour = car_colour_editText_register.text.toString()

                        model.insurer = insurance_name_editText_register.text.toString()
                        model.insuranceType = insurance_cover_editText_register.text.toString()
                        model.insuranceExpiry = insurance_expiry_editText_register.text.toString().toInt()

                        model.insuranceDocuments = photos

                        userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .setValue(model)

                        val intent = Intent(this, LoginUser::class.java)

                        startActivity(intent)

                        finish()
                    }
                }
    }
}