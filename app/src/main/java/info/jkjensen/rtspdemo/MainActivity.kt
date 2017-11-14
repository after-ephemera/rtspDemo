package info.jkjensen.rtspdemo

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.rtsp.RtspServer
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.SurfaceHolder
import net.majorkernelpanic.streaming.Session
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.WindowManager
import net.majorkernelpanic.streaming.video.VideoQuality
import java.lang.Exception
import android.net.wifi.WifiManager
import android.content.Context.WIFI_SERVICE




class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, Session.Callback {
    var session: Session? = null
    var session2: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200)

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(RtspServer.KEY_PORT, 1234.toString())
        editor.commit()

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("multicastLock")
        multicastLock.setReferenceCounted(false)
        multicastLock.acquire()

        val sessionBuilder = SessionBuilder.getInstance()
                .setSurfaceView(surfaceView)
                .setCamera(1)
                .setPreviewOrientation(90)
                .setContext(applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoQuality(VideoQuality(320,240,20,50000000))
//                .setDestination("192.168.43.19")// mbp
//                .setDestination("192.168.43.20")// iMac
//                .setDestination("192.168.43.154")// mbp?
                .setDestination("232.0.1.2") // multicast
                .setCallback(this)
        sessionBuilder.videoEncoder = SessionBuilder.VIDEO_H264
        session = sessionBuilder.build()
        session!!.configure()

//        val sessionBuilder2 = SessionBuilder.getInstance()
//                .setSurfaceView(surfaceView)
//                .setCamera(1)
//                .setPreviewOrientation(90)
//                .setContext(applicationContext)
//                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
//                .setVideoQuality(VideoQuality(320,240,20,50000000))
//                .setDestination("192.168.43.19")// mbp
////                .setDestination("192.168.43.20")// iMac
//                .setCallback(this)
//        sessionBuilder2.videoEncoder = SessionBuilder.VIDEO_H264
//        session2 = sessionBuilder.build()
//        session2!!.configure()

        // Starts the RTSP server
        startService(Intent(this, RtspServer::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("Main", requestCode.toString())
    }

    override fun onStop() {
        super.onStop()
        // Stops the RTSP server
        stopService(Intent(this, RtspServer::class.java))
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        session!!.startPreview()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}

    override fun surfaceDestroyed(p0: SurfaceHolder?) {}

    override fun onBitrateUpdate(bitrate: Long) {
        Log.d("Main", "Bitrate update: " + bitrate.toString())
    }

    override fun onSessionError(reason: Int, streamType: Int, e: Exception?) {
        Log.d("Main", "Error")
        e?.printStackTrace()
    }

    override fun onPreviewStarted() {
        Log.d("Main", "Preview started")
    }

    override fun onSessionConfigured() {
        Log.d("Main", "Sesh configured")
        session!!.start()
    }

    override fun onSessionStarted() {
        Log.d("Main", "Sesh started")
    }

    override fun onSessionStopped() {
        Log.d("Main", "Sesh stopped")
    }

}
