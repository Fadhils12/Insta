package com.example.insta

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.insta.model.User
import com.example.insta.ui.profile.ProfileFragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_setting_account.*

class SettingAccountActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var cekInfoProfile = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_account)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

//        logout_btn_setprofile.setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//
//            val intent = Intent(this@SettingAccountActivity, LoginActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            finish()
//        }

        btn_close_setting_profile.setOnClickListener {
            val intent = Intent(this@SettingAccountActivity, MainActivity::class.java)
            startActivity(intent)
        }

        delete_account_btn_setprofile.setOnClickListener {

            onDelete()

//            firebaseUser.delete()
//                .addOnSuccessListener {
//                }
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Log.d(TAG, "User account deleted.")
//                    }
//                }
//
//            val intent = Intent(this@SettingAccountActivity, LoginActivity::class.java)
//            startActivity(intent)

        }

        change_setimage_text.setOnClickListener {
            cekInfoProfile = "clicked"

            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@SettingAccountActivity)
        }

        save_info_profile_btn.setOnClickListener {
            if (cekInfoProfile == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
        userInfo()
    }

    private fun onDelete() {
        AlertDialog.Builder(this) // ketika tombol DELETE diklik, AlertDialog akan muncul
            .setTitle("Delete Account") // Title AlertDialog
            .setMessage("This will delete your Profile Information. Are you sure?") // pesan info
            .setPositiveButton("Yes") { dialog, which -> // button Yes
//                firebaseUser.delete()
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful){
//                            Log.d(TAG, "User account deleted")
//                        }
//                    }


                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this@SettingAccountActivity, LoginActivity::class.java)
//                startActivity(intent)

//                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
//                finish()
            }
            .setNegativeButton("No") { dialog, which ->

            }
            .setCancelable(false) // AlertDialog tidak dapat hilang kecuali menekan buton Yes/No
            .show() // memunculkan AlertDialog
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_LONG)
                .show()
            TextUtils.isEmpty(fullname_setprofile_edittext.text.toString()) -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            username_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            bio_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("ACCOUNT SETTING")
                progressDialog.setMessage("Please wait..., we are updating profile..")
                progressDialog.show()

                val fileRef = storageProfilePictureRef!!.child(firebaseUser!!.uid + "jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {

                        task.exception.let {
                            throw it!!
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                })
                    .addOnCompleteListener(OnCompleteListener<Uri> { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            myUrl = downloadUrl.toString()

                            val ref = FirebaseDatabase.getInstance().reference.child("Accounts")

                            val userMap = HashMap<String, Any>()

                            userMap["fullname"] =
                                fullname_setprofile_edittext.text.toString().toLowerCase()
                            userMap["username"] =
                                username_setprofile_edittext.text.toString().toLowerCase()
                            userMap["bio"] =
                                bio_setprofile_edittext.text.toString().toLowerCase()
                            userMap["image"] = myUrl

                            ref.child(firebaseUser.uid).updateChildren(userMap)

                            Toast.makeText(
                                this,
                                "Info Profile has been update",
                                Toast.LENGTH_LONG
                            ).show()

                            val intent =
                                Intent(this@SettingAccountActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            progressDialog.dismiss()
                        } else {
                            progressDialog.dismiss()
                        }
                    })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            setprofile_image_view.setImageURI(imageUri)
        } else {

        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.ic_profile)
                        .into(setprofile_image_view)
                    username_setprofile_edittext.setText(user.getUsername())
                    fullname_setprofile_edittext.setText(user.getFullname())
                    bio_setprofile_edittext.setText(user.getBio())
                }
            }
        })
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(fullname_setprofile_edittext.text.toString()) -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            username_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            bio_setprofile_edittext.text.toString() == "" -> {
                Toast.makeText(this, "Please dont be empty..", Toast.LENGTH_LONG).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference
                    .child("Accounts")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = fullname_setprofile_edittext.text.toString().toLowerCase()
                userMap["username"] = username_setprofile_edittext.text.toString().toLowerCase()
                userMap["bio"] = bio_setprofile_edittext.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Info Profile has been update", Toast.LENGTH_LONG).show()

                val intent = Intent(this@SettingAccountActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}