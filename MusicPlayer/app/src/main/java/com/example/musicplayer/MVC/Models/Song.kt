package com.example.musicplayer.MVC.Models

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.widget.Toast

import android.R
import android.media.AudioManager
import android.media.MediaPlayer.OnCompletionListener

import android.os.Bundle
import android.view.View
import android.widget.Button
import java.io.Serializable


class Song(var title: String, var author: String, var path: String, var songImage: Bitmap?) : Serializable {
}