package info.jkjensen.rtspdemo

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_server.*
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
import android.util.DisplayMetrics
import net.majorkernelpanic.streaming.MediaStream
import net.majorkernelpanic.streaming.gl.SurfaceView


class ServerActivity : AppCompatActivity(), SurfaceHolder.Callback, Session.Callback {
    var session: Session? = null
    var session2: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
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

        val displayMetrics:DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height:Int = displayMetrics.heightPixels
        val width:Int = displayMetrics.widthPixels

        val sessionBuilder = SessionBuilder.getInstance()
                .setSurfaceView(surfaceView)
                .setCamera(0)
                .setPreviewOrientation(90)
                .setContext(applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                //Supposedly supported resolutions: 1920x1080, 1600x1200, 1440x1080, 1280x960, 1280x768, 1280x720, 1024x768, 800x600, 800x480, 720x480, 640x480, 640x360, 480x640, 480x360, 480x320, 352x288, 320x240, 240x320, 176x144, 160x120, 144x176

//                .setVideoQuality(VideoQuality(320,240,30,2000000)) // Supported
//                .setVideoQuality(VideoQuality(640,480,30,2000000)) // Supported
//                .setVideoQuality(VideoQuality(720,480,30,2000000)) // Supported
//                .setVideoQuality(VideoQuality(800,600,30,2000000)) // Supported
//                .setVideoQuality(VideoQuality(1024,768,30,4000000)) // Supported
                .setVideoQuality(VideoQuality(1280,960,4,8000000)) // Supported
//                .setDestination("192.168.43.19")// mbp
//                .setDestination("192.168.43.20")// iMac
//                .setDestination("192.168.43.19")// mbp
//                .setDestination("192.168.43.110")// Galaxy s7
                .setDestination("192.168.43.6")// OnePlus 5
//                .setDestination("232.0.1.2") // multicast
                .setCallback(this)
        sessionBuilder.videoEncoder = SessionBuilder.VIDEO_H264
        session = sessionBuilder.build()
        session!!.videoTrack.streamingMethod = MediaStream.MODE_MEDIACODEC_API
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)


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

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("ServerActivity", requestCode.toString())
        session!!.configure()
        // Starts the RTSP server
        startService(Intent(this, RtspServer::class.java))
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
        Log.d("ServerActivity", "Bitrate update: " + bitrate.toString())
    }

    override fun onSessionError(reason: Int, streamType: Int, e: Exception?) {
        Log.d("ServerActivity", "Error")
        e?.printStackTrace()
    }

    override fun onPreviewStarted() {
        Log.d("ServerActivity", "Preview started")
    }

    override fun onSessionConfigured() {
        Log.d("ServerActivity", "Sesh configured")
        session!!.start()
    }

    override fun onSessionStarted() {
        Log.d("ServerActivity", "Sesh started")
    }

    override fun onSessionStopped() {
        Log.d("ServerActivity", "Sesh stopped")
    }

}
