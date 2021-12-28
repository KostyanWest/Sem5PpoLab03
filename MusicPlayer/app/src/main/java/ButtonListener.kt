package com.example.musicplayer

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.musicplayer.MVC.Controller.MusicManager

class ButtonListener: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {



        var action = intent?.action?: ""

        val music = MusicManager.createInstance()

        when(action){
            "com.example.app.ACTION_PLAY" -> playStop(music)
            "com.example.app.ACTION_NEXT" -> next(music)
            "com.example.app.ACTION_PREV" -> prev(music)
            else -> return
        }


    }

    private fun next(music: MusicManager){
        music.onSkipToNext()
    }

    private fun prev(music: MusicManager){
        music.onSkipToPrevious()
    }

    private fun playStop(music: MusicManager){
        val notificationView = music.getNotificationView()
        val notification = music.getNotification()
        val notificationManager = music.getNotificationManager()

        if(music.isPlaying()){
            music.pause()

            notificationView.setInt(R.id.play_btn, "setBackgroundResource", R.drawable.ic_play_arrow);
            notificationManager.notify(1, notification);
        }else{
            music.play()
            notificationView.setInt(R.id.play_btn, "setBackgroundResource", R.drawable.ic_baseline_pause_24);
            notificationManager.notify(1, notification);
        }
    }

}