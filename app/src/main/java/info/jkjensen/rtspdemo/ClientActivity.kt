package info.jkjensen.rtspdemo

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.View

import kotlinx.android.synthetic.main.activity_client.*
import net.majorkernelpanic.streaming.Session
import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspClient
import java.lang.Exception
import android.R.id.edit
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.util.Log
import net.majorkernelpanic.streaming.video.VideoQuality


class ClientActivity : AppCompatActivity(), View.OnClickListener, Session.Callback, RtspClient.Callback, SurfaceHolder.Callback {
    var session:Session? = null
    val client:RtspClient = RtspClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        toggleStreamButton.setOnClickListener(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200)

        session = SessionBuilder.getInstance()
                .setContext(applicationContext)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoQuality(VideoQuality(320,240,20,5000000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(surfaceView)
                .setCamera(1)
                .setPreviewOrientation(90)
                .setCallback(this)
                .build()

//        session?.configure()
        client.session = session
        client.setServerAddress("192.168.43.1", 1234)
        client.setCallback(this)
//        client.setTransportMode(RtspClient.TRANSPORT_UDP)

//        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW)
        surfaceView.holder.addCallback(this)
//        selectQuality()

    }

    override fun onDestroy() {
        super.onDestroy()
        client.release()
        session?.release()
        surfaceView.holder.removeCallback(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.toggleStreamButton ->{
                toggleStream()
            }
        }
    }

    fun toggleStream(){

        if (!client.isStreaming) {
            client.startStream()

        } else {
            // Stops the stream and disconnects from the RTSP server
            client.stopStream()
        }
    }

    // CLIENT CALLBACK

    override fun onRtspUpdate(message: Int, exception: Exception?) {
        when(message){
            RtspClient.ERROR_CONNECTION_FAILED, RtspClient.ERROR_WRONG_CREDENTIALS -> {
                exception?.printStackTrace()
            }
        }
    }

    // SESSION CALLBACKS

    override fun onBitrateUpdate(bitrate: Long) {
        bitrateTV.text = getString(R.string.bitrate) + bitrate
    }

    override fun onSessionError(reason: Int, streamType: Int, e: Exception?) {
    }

    override fun onPreviewStarted() {
        Log.d("ClientActivity", "Preview Started")
//        if(session?.camera == Camera.CameraInfo.CAMERA_FACING_FRONT){
//
//        }
    }

    override fun onSessionConfigured() {
    }

    override fun onSessionStarted() {
    }

    override fun onSessionStopped() {
    }

    // SURFACE CALLBACKS

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        client.stopStream()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        session?.startPreview()
    }

}
