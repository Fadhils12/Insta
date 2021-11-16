package com.example.insta.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.insta.R
import com.example.insta.model.Post
import com.squareup.picasso.Picasso

class ImgPostAdapter(private val mContext: Context, mPost: List<Post>)
    : RecyclerView.Adapter<ImgPostAdapter.ViewHolder?>() {

    private var mPost: List<Post>? = null

    init {
        this.mPost = mPost
    }

    class ViewHolder(@NonNull itemView: View)
        :RecyclerView.ViewHolder(itemView) {

        var postImage: ImageView = itemView.findViewById(R.id.post_image_grid)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgPostAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.img_post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImgPostAdapter.ViewHolder, position: Int) {
        val post: Post = mPost!![position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }
}