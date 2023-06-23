package com.pdm.pdmmusicapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.net.URL


class MyNotificationManager(context: Context) {
    private var _context: Context = context
    fun SendNotification(level: String, url: String?) {

        try {
            val alertUrl: Uri;
            when (level) {
                "1" -> {
                    alertUrl =
                        Uri.parse("android.resource://" + _context.applicationContext.packageName + "/" + R.raw.pdmhello)
                }

                "2" -> {
                    alertUrl =
                        Uri.parse("android.resource://" + _context.applicationContext.packageName + "/" + R.raw.pdmbye)
                }

                "3" -> {
                     alertUrl =
                     Uri.parse("android.resource://" + _context.applicationContext.packageName + "/" + R.raw.doortype)

                }

                "4" -> {
                    val alertUrl2 = URL(url)
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(alertUrl2.toString())
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    return
                }

                else -> {
                    return
                }
            }
            //val r = RingtoneManager.getRingtone(_context, alertUrl)
            //r.play()

            val media = MediaPlayer.create(_context, alertUrl)
            media.start()

        } catch (ex: Exception) {
            Log.e("send notification", ex.message.toString())
            //ignore
        }

    }
}