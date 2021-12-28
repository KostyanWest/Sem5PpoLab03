package com.example.musicplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MVC.Controller.MUtils
import com.example.musicplayer.MVC.Controller.MusicManager
import com.example.musicplayer.MVC.Views.CustomRecyclerAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.musicplayer.MVC.Models.Song


class MainActivity : AppCompatActivity() {

    companion object {

        var mSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        private var mProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )


    }
    private lateinit var mAdapter: CustomRecyclerAdapter
    private lateinit var musicManager: MusicManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        runPermission()


    }

    private fun runPermission(){
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_LONG).show()

                    mAdapter = CustomRecyclerAdapter()


                    val g = findViewById<RecyclerView>(R.id.recyclerView)
                    musicManager = MusicManager.createInstance(applicationContext, mAdapter, MUtils(baseContext))
                    g.layoutManager = LinearLayoutManager(baseContext)
                    g.adapter = mAdapter

                    g.addOnItemTouchListener(
                        RecyclerItemClickListener(baseContext,
                            g,
                            object : RecyclerItemClickListener.OnItemClickListener {
                                override fun onItemClick(view: View?, position: Int) {
                                    musicManager.start(position)
                                }

                                override fun onLongItemClick(view: View?, position: Int) {

                                }
                            })
                    )

                    musicManager.show()

                    var button = findViewById<Button>(R.id.btn_play)

                    button.setOnClickListener{
                        if(musicManager.isPlaying()) {
                            musicManager.pause()
                            button.setBackgroundResource(R.drawable.ic_play_arrow)
                        }
                        else{
                            musicManager.play()
                            button.setBackgroundResource(R.drawable.ic_baseline_pause_24)
                        }

                    }

                    findViewById<Button>(R.id.btn_next).setOnClickListener{
                        musicManager.onSkipToNext()
                    }

                    findViewById<Button>(R.id.btn_prev).setOnClickListener{
                        musicManager.onSkipToPrevious()
                    }

                    findViewById<RelativeLayout>(R.id.layout_collapsed).setOnClickListener {
                        if(musicManager.isPlaying())
                            startActivity(Intent(applicationContext, MusicActivity::class.java))
                    }

                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity, "R.string.storage_permission_denied_message", Toast.LENGTH_LONG).show()
                }
            }
            ).check()
    }
}


class RecyclerItemClickListener(
    context: Context?,
    recyclerView: RecyclerView,
    private val mListener: OnItemClickListener?,
) :
    OnItemTouchListener {
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongItemClick(view: View?, position: Int)
    }

    var mGestureDetector: GestureDetector
    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    init {
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

}