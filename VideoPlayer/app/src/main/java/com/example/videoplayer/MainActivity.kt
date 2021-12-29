package com.example.videoplayer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener


class Video {
    companion object{
        var currentDuration = 0
        var uri: Uri? = null
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var seekBar: SeekBar
    lateinit var currentDuration: TextView
    lateinit var duration: TextView
    lateinit var videoView: VideoView

    lateinit var updateSeekBar: Thread


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changescreen()
        setContentView(R.layout.activity_main)
        fullScreenMode(this)

        videoView = findViewById<VideoView>(R.id.exo_video)

        runPermission()

        findViewById<FrameLayout>(R.id.exo_all).setOnClickListener {
            hide()
        }
        if(Video.uri != null)
            loadVideo(Video.uri!!, Video.currentDuration)
    }


    private fun runPermission(){
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_LONG).show()

                    var loadActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val data: Intent? = result.data
                            val uri = data?.data
                            if(uri != null){

                                loadVideo(uri)

                            }
                        }
                    }


                    findViewById<ImageButton>(R.id.exo_open).setOnClickListener {
                        var myFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        myFileIntent.type = "*/*"
                        loadActivity.launch(myFileIntent)
                    }



                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_LONG).show()

                    System.exit(0)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }
            ).check()
    }

    fun getTime(milliseconds: Int): String? {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }


    private var handler = Handler()

    fun visible(){
        val frame = findViewById<FrameLayout>(R.id.exo_button)

        frame.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages(null)
    }

    fun hide(){
        val frame = findViewById<FrameLayout>(R.id.exo_button)

        visible()

        val runnable: Runnable = object : Runnable {
            override fun run() {
                frame.visibility = View.INVISIBLE
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    fun loadVideo(uri: Uri, currentDur: Int = 0){

        seekBar = findViewById(R.id.seek_bar)
        currentDuration = findViewById(R.id.exo_position)
        duration = findViewById(R.id.exo_duration)

        val playOrPause = findViewById<ImageButton>(R.id.exo_play_pause)
        playOrPause.setOnClickListener {
            if(videoView.isPlaying()) {
                videoView.pause()
                playOrPause.setBackgroundResource(R.drawable.exo_controls_play)
            }else{
                videoView.start()
                playOrPause.setBackgroundResource(R.drawable.exo_controls_pause)
            }
        }

        findViewById<ImageButton>(R.id.exo_prev).setOnClickListener{
            videoView.seekTo(videoView.currentPosition - 10000)
        }

        findViewById<ImageButton>(R.id.exo_next).setOnClickListener {
            videoView.seekTo(videoView.currentPosition + 10000)
        }
        videoView = findViewById(R.id.exo_video)

        Video.uri = uri



        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener(object : MediaPlayer.OnPreparedListener{
            override fun onPrepared(mp: MediaPlayer?) {
                seekBar.setMax(videoView.duration)
                duration.text = getTime(videoView.duration)
                videoView.start()
                hide()
                playOrPause.setBackgroundResource(R.drawable.exo_controls_pause)
            }

        })

        val handler1 = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                currentDuration.text = getTime(videoView.currentPosition)
                handler1.postDelayed(this, 1000)
            }
        }
        handler1.postDelayed(runnable, 0)

        videoView.setOnCompletionListener(object : MediaPlayer.OnCompletionListener{
            override fun onCompletion(mp: MediaPlayer?) {
                visible()
                playOrPause.setBackgroundResource(R.drawable.exo_controls_pause)
                handler1.removeCallbacksAndMessages(null)

                seekBar.progress = 0
                currentDuration.text = "00:00:00"
            }

        })


        updateSeekBar = Thread(object: Runnable
        {
            override fun run() {

                var totatlDuration = videoView.duration
                var currentPosition = 0

                while (currentPosition < totatlDuration)
                {

                    try {
                        Thread.sleep(500)
                        currentPosition = videoView.currentPosition
                        seekBar.setProgress(currentPosition)


                    }
                    catch (ex: Exception) {
                        when(ex) {
                            is InterruptedException, is  IllegalStateException -> {
                                ex.printStackTrace()
                            }
                            else -> throw ex
                        }
                    }
                }
            }
        })

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean ){

            }
            override fun onStartTrackingTouch(seekBar: SeekBar){

            }
            override fun onStopTrackingTouch(seekBar: SeekBar){
                videoView.seekTo(seekBar.progress)
            }
        })


        currentDuration.text = getTime(currentDur)
        seekBar.progress = currentDur
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            Video.currentDuration = videoView.currentPosition
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            Video.currentDuration = videoView.currentPosition
        }

        super.onConfigurationChanged(newConfig)
    }

    fun changescreen(){
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    fun fullScreenMode(activity: AppCompatActivity) {
        val decorView: View = activity.window.decorView
        val uiOptions: Int = decorView.getSystemUiVisibility()
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(newUiOptions)
    }

}