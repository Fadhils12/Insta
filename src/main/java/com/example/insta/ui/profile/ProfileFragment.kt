package com.example.insta.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insta.LoginActivity
import com.example.insta.R
import com.example.insta.SettingAccountActivity
import com.example.insta.adapter.ImgPostAdapter
import com.example.insta.model.Post
import com.example.insta.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_setting_account.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postListGrid: MutableList<Post>? = null
    var imgPostAdapter: ImgPostAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val viewProfile = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            view?.btn_edit_account?.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            cekFollowAndFollowingBtnStatus()
        }

        var recyclerViewUploadImages: RecyclerView? = null
        recyclerViewUploadImages = viewProfile.findViewById(R.id.recyclerview_upload_picimage)
        recyclerViewUploadImages?.setHasFixedSize(true)
        val linearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImages?.layoutManager = linearLayoutManager

        postListGrid = ArrayList()
        imgPostAdapter = context?.let { ImgPostAdapter(it, postListGrid as ArrayList<Post>) }
        recyclerViewUploadImages?.adapter = imgPostAdapter

        viewProfile.btn_edit_account.setOnClickListener {
            val getButtonText = view?.btn_edit_account?.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        SettingAccountActivity::class.java
                    )
                )

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Followers").child(profileId).removeValue()
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }

        viewProfile.option_logout.setOnClickListener {
//            val builder = AlertDialog.Builder(activity?.applicationContext)
//            builder.setTitle(" ")
//            builder.setIcon(R.drawable.ic_logout2)
//            builder.setMessage("Are you sure wan to Log out?")
//            builder.setPositiveButton("Yes, sure") { dialog, which ->
//                FirebaseAuth.getInstance().signOut()
//
//                val intent = Intent(activity, LoginActivity::class.java)
//                startActivity(intent)
//                activity?.finish()
//            }
//
//            builder.setNegativeButton("No") { dialog, which ->
//
//            }
//
//            val alert = builder.create()
//            alert.show()

            val dialogBuilder = AlertDialog.Builder(activity)
            dialogBuilder.setMessage(it.toString())
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes, sure", DialogInterface.OnClickListener {
                        dialog, id ->
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
                })
                .setNegativeButton("No, sure", DialogInterface.OnClickListener { dialog, id ->

                })

            val alert = dialogBuilder.create()
            alert.setTitle(" ")
            alert.setIcon(R.drawable.ic_logout_2)
            alert.setMessage("Are you sure wan to Log out?")
            alert.show()

        }

        getFollowers()
        getFollowings()
        userInfo()
        myPost()

        return viewProfile
    }

    private fun myPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postListGrid as ArrayList<Post>).clear()

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisher().equals(profileId)) {
                            (postListGrid as ArrayList<Post>).add(post!!)
                        }
                        Collections.reverse(postListGrid)
                        imgPostAdapter!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    private fun cekFollowAndFollowingBtnStatus() {

        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()) {
                        view?.btn_edit_account?.text = "Following"
                    } else {
                        view?.btn_edit_account?.text = "Follow"
                    }
                }

            })
        }

    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference()
            .child("Accounts").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.ic_profile)
                        .into(view?.img_profile)
                    view?.profile_fragment_username?.text = user?.getUsername()
                    view?.txt_full_namaProfile?.text = user?.getFullname()
                    view?.txt_bio_profile?.text = user?.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

//        userRef.addValueEventListener(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                if (p0.exists()) {
//                    val user = p0.getValue<User>(User::class.java)
//
//                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.ic_profile)
//                        .into(view?.img_profile)
//                    view?.profile_fragment_username?.text = user?.getUsername()
//                    view?.txt_full_namaProfile?.text = user?.getFullname()
//                    view?.txt_bio_profile?.text = user?.getBio()
//                }
//            }
//
//        })
    }

    private fun getFollowings() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.txt_totalFollowing?.text = p0.childrenCount.toString()
                }
            }

        })

    }

    private fun getFollowers() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.txt_totalFollowers?.text = p0.childrenCount.toString()
                }
            }

        })

    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

}