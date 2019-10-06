package br.ufpe.cin.android.podcast

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import br.ufpe.cin.android.podcast.models.ItemFeed
import br.ufpe.cin.android.podcast.models.PodcastDB
import org.jetbrains.anko.doAsync
import java.io.File

class PodcastPlayer {

    private var mPlayer: MediaPlayer? = null

    fun isPlaying() = mPlayer?.isPlaying ?: false

    fun startPlayer() {
        if (mPlayer != null) {
            if (!mPlayer!!.isPlaying) {
                mPlayer!!.start()
            }
        }
    }

    fun pausePlayer(): Int {
        if (mPlayer != null) {
            if (mPlayer!!.isPlaying) {
                mPlayer!!.pause()
                return mPlayer!!.currentPosition
            }
        }
        return 0
    }

    fun preparePlayer(ctx: Context, itemFeed: ItemFeed) {
        mPlayer = MediaPlayer.create(ctx, Uri.parse(itemFeed.mp3Path))

        mPlayer?.setOnCompletionListener {
            val mp3Path = itemFeed.mp3Path
            itemFeed.mp3Path = null
            itemFeed.seekPosition = 0

            val mp3File = File(mp3Path!!)
            if (mp3File.exists()) {
                mp3File.delete()
            }

            doAsync {
                val feedItemDao = PodcastDB.getDatabase(ctx).itemFeedDAO()

                feedItemDao.updateFeedItem(itemFeed)
            }

        }
    }

    fun releasePlayer() {
        mPlayer?.release()
        mPlayer = null
    }

    fun startFromPosition(startPosition: Int) {
        mPlayer?.seekTo(startPosition)
        mPlayer?.start()
    }

    companion object {

        private var INSTANCE: PodcastPlayer? = null

        fun getInstance(): PodcastPlayer {

            if (INSTANCE == null) {
                synchronized(PodcastPlayer::class) {
                    INSTANCE = PodcastPlayer()
                }
            }

            return this.INSTANCE!!
        }
    }
}