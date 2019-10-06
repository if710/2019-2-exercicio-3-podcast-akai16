package br.ufpe.cin.android.podcast.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.PodcastPlayer
import br.ufpe.cin.android.podcast.receivers.PlayerReceiver
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.models.PodcastDB
import br.ufpe.cin.android.podcast.views.MainActivity
import org.jetbrains.anko.doAsync


class PlayPodcastService : Service() {

    private var mItemPlaying: String? = null

    private val mBinder = MusicBinder()
    private val mPlayer = PodcastPlayer.getInstance()

    private val pauseIntentFilter = IntentFilter(Consts.ACTION_PAUSE_PLAYER)
    private val playerReceiver = PlayerReceiver()

    override fun onCreate() {

        super.onCreate()

        registerReceiver(playerReceiver, pauseIntentFilter)

        createChannel()
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@PlayPodcastService, 0, notificationIntent, 0)


        val pauseIntent = Intent(this@PlayPodcastService, PlayerReceiver::class.java).apply {
            action = Consts.ACTION_PAUSE_PLAYER
        }
        val pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, 0)

        val playIntent = Intent(this@PlayPodcastService, PlayerReceiver::class.java).apply {
            action = Consts.ACTION_PLAY_PLAYER
        }
        val playPendingIntent = PendingIntent.getBroadcast(this, 2, playIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext, "1"
        )
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("Podcast tocando")
            .setContentText("Clique para acessar o player!")
            .addAction(android.R.drawable.ic_media_pause, "Pausar", pausePendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun startPlayer(feedItemId: Int) {

        doAsync {

            val feedItemDAO = PodcastDB.getDatabase(this@PlayPodcastService).itemFeedDAO()

            val itemFeed = feedItemDAO.getFeedItemByID(feedItemId)

            if (itemFeed.mp3Path!! != mItemPlaying) {

                if (!mPlayer.isPlaying()) {
                    mPlayer.preparePlayer(this@PlayPodcastService, itemFeed)
                    mItemPlaying = itemFeed.mp3Path!!
                }
            }

            val startPosition = itemFeed.seekPosition ?: 0


            mPlayer.startFromPosition(startPosition)

            if (!mPlayer.isPlaying()) {
                Toast.makeText(
                    this@PlayPodcastService,
                    "Erro ao abrir MP3, Corrompido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    fun pausePlayer(feedItemId: Int) {
        doAsync {
            val curPosition = mPlayer.pausePlayer()

            val podcastDB = PodcastDB.getDatabase(this@PlayPodcastService).itemFeedDAO()
            val feedItem = podcastDB.getFeedItemByID(feedItemId)

            feedItem.seekPosition = curPosition

            podcastDB.updateFeedItem(feedItem)
        }
    }

    fun isPlaying(): Boolean {
        return mPlayer.isPlaying()
    }

    override fun onDestroy() {
        this.mPlayer.releasePlayer()
        this.unregisterReceiver(this.playerReceiver)
        super.onDestroy()
    }

    inner class MusicBinder : Binder() {
        internal val service: PlayPodcastService
            get() = this@PlayPodcastService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return this.mBinder
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                "1",
                "Canal de Notificacoes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "Canal do Podcast"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }



}
