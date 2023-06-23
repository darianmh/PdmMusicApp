package com.pdm.pdmmusicapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.StrictMode
import android.util.Log
import com.beust.klaxon.Klaxon
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors


class ClientClass(context: Context) : Thread() {

    val hostAddress: String = "109.122.199.199"

    //private val hostAddress: String = "192.168.1.142"
    private val hostPort: Int = 7086
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    var isConnected: Boolean = false
    private var socket: Socket? = null

    private val _context: Context = context
    private val notificationManager: MyNotificationManager =
        MyNotificationManager(_context)

    fun write(text: String) {
        try {
            outputStream?.write(text.toByteArray())
        } catch (ex: Exception) {
            //ignore
        }
    }

    public override fun run() {
        Log.i("back service", "ClientClass")
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        StartSocket(_context)
    }

    fun StartSocket(myContext: Context) {
        isConnected = false
        try {
            socket = Socket()
            socket!!.connect(InetSocketAddress(hostAddress, hostPort), 500)
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
            isConnected = true
        } catch (ex: Exception) {
            Log.e("Socket connection error", ex.message.toString())
        }
        if (!isConnected) StartSocket(myContext)
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        try {
            val UId = "<UId></UId>"
            outputStream!!.write(UId.toByteArray())
            val buffer = ByteArray(1024)
            var byte: Int
            while (true) {
                try {
                    if (socket!!.isClosed || !socket!!.isConnected) {
                        val newClient: ClientClass = ClientClass(myContext)
                        newClient.run()
                        break
                    }
                    if (inputStream == null) continue
                    byte = inputStream!!.read(buffer)
                    if (byte > 0) {
                        val finalBytes = byte
                        handler.post {
                            kotlin.run {
                                try {
                                    val tmpMassage = String(buffer, 0, finalBytes)
                                    val model = Klaxon().parse<AlertViewModel>(tmpMassage)
                                    if (model == null) return@run
                                    notificationManager.SendNotification(
                                        model.Level,
                                        model.Url
                                    )
                                    val pm =
                                        myContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                                    val isScreenOn =
                                        pm.isInteractive  // check if screen is on

                                    if (!isScreenOn) {
                                        val wl = pm.newWakeLock(
                                            PowerManager.PARTIAL_WAKE_LOCK,
                                            "myApp:notificationLock"
                                        )
                                        wl.acquire(3000) //set your time in milliseconds
                                    }
                                    Log.i("client class", tmpMassage)
                                } catch (ex: Exception) {
                                    Log.e("receive message", ex.message.toString())
                                    //ignore
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("receive message", ex.message.toString())
                    StartSocket(myContext)
                    //ignore
                }
            }

//            executor.execute(kotlinx.coroutines.Runnable {
//                kotlin.run {
//
//                }
//            })
        } catch (ex: Exception) {
            Log.e("receive message", ex.message.toString())
            StartSocket(myContext)
            //ignore
        }
    }

}