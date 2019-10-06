package br.ufpe.cin.android.podcast.receivers

import android.content.*
import android.os.IBinder
import android.util.Log
import br.ufpe.cin.android.podcast.PodcastPlayer
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.services.PlayPodcastService

class PlayerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val mPlayer = PodcastPlayer.getInstance()

        if (intent.action == Consts.ACTION_PLAY_PLAYER) {
            if (!mPlayer.isPlaying()) {
                mPlayer.startPlayer()
            }
        }

        if (intent.action == Consts.ACTION_PAUSE_PLAYER) {
            if (mPlayer.isPlaying()) {
                mPlayer.pausePlayer()
            }
        }
    }
}
