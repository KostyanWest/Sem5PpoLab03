package com.example.musicplayer.MVC.Controller

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC
import androidx.core.database.getStringOrNull
import com.example.musicplayer.MVC.Models.Song
import com.example.musicplayer.R
import java.util.ArrayList

class MUtils(private val context: Context) {

    companion object{
        private val baseProjection = arrayOf(
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ARTIST
        )
    }


    fun songsByFilePath(filePath: Array<String>): List<Song> {
        return songs(
            makeSongCursor(
                MediaStore.Audio.AudioColumns.DATA + "=?",
                filePath
            )
        )
    }


        private fun makeSongCursor(

            selection: String?,
            selectionValues: Array<String>?
        ): Cursor? {
            var selectionFinal = selection
            var selectionValuesFinal = selectionValues
            selectionFinal = if (selection != null && selection.trim { it <= ' ' } != "") {
                "$IS_MUSIC AND $selectionFinal"
            } else {
                IS_MUSIC
            }

            selectionFinal =
                selectionFinal + " AND " + MediaStore.Audio.AudioColumns.DATA + " LIKE ?"
            selectionValuesFinal = addSelectionValues(
                selectionValuesFinal, arrayListOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).canonicalPath
                )
            )
            selectionFinal =
                selectionFinal + " AND " + MediaStore.Audio.Media.DURATION + ">= " + 20000

            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            return try {
                context.contentResolver.query(
                    uri,
                    baseProjection,
                    selectionFinal,
                    selectionValuesFinal,
                    null
                )
            } catch (ex: SecurityException) {
                return null
            }
        }

    fun songs(): List<Song> {
        return songs(makeSongCursor(null, null))
    }


        fun songs(cursor: Cursor?): List<Song> {
            val songs = arrayListOf<Song>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    songs.add(getSongFromCursorImpl(cursor))
                } while (cursor.moveToNext())
            }
            cursor?.close()
            return songs
        }





        fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap =
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }


        private fun getAlbumImage(path: String): Bitmap {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(path)
            val data = mmr.embeddedPicture
            var id = context.resources.getIdentifier("ic_action_name","drawable", context.packageName)
            return if (data != null)
                BitmapFactory.decodeByteArray(data, 0, data.size)
            else
                drawableToBitmap(context.resources.getDrawable(R.drawable.ic_action_name, context.theme))
        }

        @SuppressLint("Range")
        private fun getSongFromCursorImpl(
            cursor: Cursor
        ): Song {
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA))
            val artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST))
            return Song(
                title,
                artistName ?: "",
                data ?: "",
                getAlbumImage(data)
            )
        }

    private fun addSelectionValues(
        selectionValues: Array<String>?,
        paths: ArrayList<String>
    ): Array<String> {
        var selectionValuesFinal = selectionValues
        if (selectionValuesFinal == null) {
            selectionValuesFinal = emptyArray()
        }
        val newSelectionValues = Array(selectionValuesFinal.size + paths.size) {
            "n = $it"
        }
        System.arraycopy(selectionValuesFinal, 0, newSelectionValues, 0, selectionValuesFinal.size)
        for (i in selectionValuesFinal.size until newSelectionValues.size) {
            newSelectionValues[i] = paths[i - selectionValuesFinal.size] + "%"
        }
        return newSelectionValues
    }
}