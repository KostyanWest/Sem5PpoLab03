package com.example.musicplayer.MVC.Views

import android.content.Context
import android.graphics.Color
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MVC.Models.Song
import com.example.musicplayer.R
import com.google.android.material.textview.MaterialTextView

class CustomRecyclerAdapter(private val songs: ArrayList<Song> = arrayListOf()):
    RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var layout: ConstraintLayout? = null
        var image: AppCompatImageView? = null
        var title: MaterialTextView? = null
        var author: MaterialTextView? = null

        init {
            layout = itemView.findViewById(R.id.layout)
            image = itemView.findViewById(R.id.image)
            title = itemView.findViewById(R.id.title)
            author = itemView.findViewById(R.id.text)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.image?.setImageBitmap(songs[position].songImage)
        holder.title?.text = songs[position].title
        holder.author?.text = songs[position].author
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun addAll(allSongs: List<Song>){
        songs.addAll(allSongs)
        notifyDataSetChanged()
    }

    fun getSong(position: Int): Song{
        return songs[position]
    }
}