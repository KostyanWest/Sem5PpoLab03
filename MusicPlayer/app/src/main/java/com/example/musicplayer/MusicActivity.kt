package com.example.musicplayer

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.MVC.Controller.MusicManager
import android.widget.SeekBar.OnSeekBarChangeListener
import java.lang.Thread.sleep
import java.time.Duration
import android.view.animation.Animation
import android.view.GestureDetector

import android.view.MotionEvent

import android.view.GestureDetector.SimpleOnGestureListener








class MusicActivity : AppCompatActivity() {

    lateinit var musicManager : MusicManager

    lateinit var updateSeekBar: Thread

    lateinit var flipper: ViewFlipper
    lateinit var animFlipInForward: Animation
    lateinit var animFlipOutForward: Animation
    lateinit var animFlipInBackward: Animation
    lateinit var animFlipOutBackward: Animation


    private fun SwipeLeft() {
        flipper.inAnimation = animFlipInBackward
        flipper.outAnimation = animFlipOutBackward
        flipper.showPrevious()
        nextMusic()
    }

    private fun SwipeRight() {
        flipper.inAnimation = animFlipInForward
        flipper.outAnimation = animFlipOutForward
        flipper.showNext()
        prevMusic()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    var simpleOnGestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float,
            velocityY: Float,
        ): Boolean {
            val sensitvity = 50f
            if (e1.x - e2.x > sensitvity) {
                SwipeLeft()
            } else if (e2.x - e1.x > sensitvity) {
                SwipeRight()
            }
            return true
        }
    }

    var gestureDetector = GestureDetector(baseContext,
        simpleOnGestureListener)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_music)



        flipper = findViewById<ViewFlipper>(R.id.viewflipper);

        animFlipInForward = AnimationUtils.loadAnimation(this, R.anim.flipin);
        animFlipOutForward = AnimationUtils.loadAnimation(this, R.anim.flipout);
        animFlipInBackward = AnimationUtils.loadAnimation(this,
            R.anim.flipin_reverse);
        animFlipOutBackward = AnimationUtils.loadAnimation(this,
            R.anim.flipout_reverse);




        findViewById<Button>(R.id.back_btn).setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

        musicManager = MusicManager.createInstance()
        var b = findViewById<Button>(R.id.btn_play)
        b.setOnClickListener {
            if(musicManager.isPlaying()) {
                musicManager.pause()
                b.setBackgroundResource(R.drawable.ic_play_arrow)
            }
            else{
                musicManager.play()
                b.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            }
        }

        showMusic()
        musicManager.onEndMusic = Runnable {
            nextMusic()
        }

        findViewById<Button>(R.id.btn_next).setOnClickListener {
            musicManager.onSkipToNext()
            showMusic()
        }

        findViewById<Button>(R.id.btn_prev).setOnClickListener {
            musicManager.onSkipToPrevious()
            showMusic()
        }

        var start = findViewById<TextView>(R.id.song_start)
        start.text = "0:00"

        var song = musicManager.getPlayingSong()!!

        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                start.text = getTime(musicManager.getCurrentPosition())

                if(musicManager.getPlayingSong()!!.title != song.title)
                    showMusic()

                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 0)

    }

    fun getTime(duration: Int) : String{
        var time = ""
        var min: Int = duration/1000/60
        var sec: Int = duration/1000%60
        time = min.toString() + ":"
        if(sec<10)
            time+="0"
        time+=sec
        return time
    }

    override fun onDestroy() {
        musicManager.onEndMusic = null
        super.onDestroy()
    }
    fun nextMusic(){
        musicManager.onSkipToNextWithoutEx()
        showMusic()
    }

    fun prevMusic(){
        musicManager.onSkipToPrevious()
        showMusic()
    }

    fun showMusic(){

        var song = musicManager.getPlayingSong()!!
        findViewById<TextView>(R.id.text).text = song.title

        findViewById<TextView>(R.id.song_end).text = getTime(musicManager.getDuration())

        findViewById<ImageView>(R.id.image).setImageBitmap(song.songImage)

        var seek = findViewById<SeekBar>(R.id.seek_bar)

        updateSeekBar = Thread(object: Runnable
        {
            override fun run() {

                var totatlDuration = musicManager.getDuration()
                var currentPosition = 0

                while (currentPosition < totatlDuration)
                {

                    try {
                        sleep(500)
                        currentPosition = musicManager.getCurrentPosition()
                        seek.setProgress(currentPosition)

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

        /* findViewById<com.gauravk.audiovisualizer.visualizer.BarVisualizer>(R.id.blast)*/

        seek.max = musicManager.getDuration()

        updateSeekBar.start()

        seek.progressDrawable.setColorFilter(resources.getColor(R.color.av_deep_orange), PorterDuff.Mode.MULTIPLY)

        seek.thumb.setColorFilter(resources.getColor(R.color.av_deep_orange), PorterDuff.Mode.SRC_IN)

        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean ){

            }
            override fun onStartTrackingTouch(seekBar: SeekBar){

            }
            override fun onStopTrackingTouch(seekBar: SeekBar){
                musicManager.onSeekTo(seekBar.progress)
            }
        })
    }
}
