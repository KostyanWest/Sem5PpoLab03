package com.example.musicplayer.MVC.Controller

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.example.musicplayer.MVC.Models.Song
import com.example.musicplayer.MVC.Views.CustomRecyclerAdapter

import android.content.Intent

import android.R
import android.app.Notification

import android.app.PendingIntent

import com.example.musicplayer.MainActivity
import android.widget.RemoteViews

import androidx.core.app.NotificationCompat

import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Build
import com.example.musicplayer.ButtonListener


class MusicManager
{
    private lateinit var context: Context
    private lateinit var adapter: CustomRecyclerAdapter
    private lateinit var utils: MUtils
    private var isStart = false

    var onEndMusic: Runnable? = null

    private lateinit var notificationView: RemoteViews
    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: Notification

    private constructor(context: Context, adapter: CustomRecyclerAdapter, utils: MUtils){
        this.context = context
        this.adapter = adapter
        this.utils = utils
    }

    companion object{
        var instance: MusicManager? = null

        fun createInstance(): MusicManager{
            return instance!!
        }

        fun createInstance(context: Context, adapter: CustomRecyclerAdapter, utils: MUtils): MusicManager{
            if (instance == null) {
                instance = MusicManager(context, adapter, utils)
            }
            else{
                instance!!.context = context
                instance!!.adapter = adapter
                instance!!.utils = utils
            }
            instance!!.loadSongs()
            return instance!!
        }
    }

    fun show(){
        adapter.notifyDataSetChanged()
    }


    private lateinit var songPos: ArrayList<Int>
    private var songIndex = 0
    private var mediaPlayer = MediaPlayer()

    private fun loadSongs(){
        adapter.addAll(utils.songs())
        songPos = (0..adapter.itemCount - 1).toList() as ArrayList<Int>
    }

    fun isPlaying(): Boolean{
        return mediaPlayer.isPlaying
    }

    fun shuffle(){
        songPos.shuffle()
        start()
    }

    fun sortStart(){
        songPos.sort()
        start()
    }
    fun start(){
        startByPos(0)
    }

    fun start(position: Int){
        startByPos(songPos.indexOf(position))
    }

    private fun startByPos(position: Int){
        songIndex = position
        start(adapter.getSong(songPos[position]))
    }

    fun makePendingIntent(name: String?): PendingIntent? {
        val intent = Intent(context, ButtonListener::class.java)
        intent.action = name
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun setNotification(songName: String?) {
        val ns = NOTIFICATION_SERVICE
        notificationManager = context.getSystemService(ns) as NotificationManager
        notificationView = RemoteViews(context.packageName, com.example.musicplayer.R.layout.activity_notify)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingNotificationIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        notificationView.setOnClickPendingIntent(com.example.musicplayer.R.id.play_btn, makePendingIntent("com.example.app.ACTION_PLAY"))
        notificationView.setOnClickPendingIntent(com.example.musicplayer.R.id.next_btn, makePendingIntent("com.example.app.ACTION_NEXT"))
        notificationView.setOnClickPendingIntent(com.example.musicplayer.R.id.prev_btn, makePendingIntent("com.example.app.ACTION_PREV"))
        notificationView.setTextViewText(com.example.musicplayer.R.id.notify_text, songName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("id123545", "My channel",
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = "My channel description"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
        notification = NotificationCompat.Builder(context, "id123545")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(Notification.PRIORITY_MAX)
            .setWhen(0)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingNotificationIntent)
            .setCustomContentView(notificationView)
            .setAutoCancel(false).build()

        notificationManager.notify(1, notification)
    }

    fun start(song: Song){
        setNotification(song.title)

        stop()
        isStart = true
        mediaPlayer = MediaPlayer.create(context, song.path.toUri())
        mediaPlayer.setOnCompletionListener {
            stop()
            onEndMusic?.run()
        }
        play()
    }


    fun play(){
        mediaPlayer.start()
    }

    fun stop(){
        isStart = false
        mediaPlayer.release()
    }

    fun pause(){
        mediaPlayer.pause()
    }

    fun onSeekTo(pos: Int) {
        mediaPlayer.seekTo(pos.toInt())
    }

    fun onSkipToNext() {
        if(isStart)
            onSkipToNextWithoutEx()
    }

    fun onSkipToNextWithoutEx() {
        startByPos(songIndex + 1)
    }

    fun onSkipToPrevious() {
        if(songIndex != 0 && isStart)
            startByPos(songIndex - 1)
    }

    fun getPlayingSong() : Song? {
        return if(isStart) adapter.getSong(songPos[songIndex]) else null
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun getNotificationView(): RemoteViews  {
        return notificationView
    }

    fun getNotification(): Notification {
        return notification
    }

    fun getNotificationManager(): NotificationManager {
        return notificationManager
    }

}