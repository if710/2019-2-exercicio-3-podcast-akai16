package br.ufpe.cin.android.podcast.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.ufpe.cin.android.podcast.consts.Consts
import br.ufpe.cin.android.podcast.models.PodcastDB
import br.ufpe.cin.android.podcast.utils.Parser
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val ACTION_RSS_DOWNLOAD = "br.ufpe.cin.android.podcast.services.action.FOO"


class RSSFeedDownloadService : IntentService("RSSFeedDownloadService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_RSS_DOWNLOAD -> {
                handleActionDownload()
            }
        }
    }


    private fun handleActionDownload() {

        val httpCon = (URL(Consts.RSS_FEED_URL).openConnection() as HttpURLConnection)

        val feedStr: String?

        val itemFeedDB = PodcastDB.getDatabase(this@RSSFeedDownloadService)

        try {

            val inStream = BufferedInputStream(httpCon.inputStream)
            feedStr = inStream.bufferedReader().readText()

            Parser.parse(feedStr).forEach {
                val oldItemFeed = itemFeedDB.itemFeedDAO().getItemByLink(it.link)

                if (oldItemFeed == null) {
                    itemFeedDB.itemFeedDAO().addFeedItem(it)
                }
            }

        } catch (e: IOException) {
            Log.d(Consts.DEBUG_TAG, "Erro ao baixar RSS")
        } finally {
            httpCon.disconnect()
        }

    }


    companion object {

        @JvmStatic
        fun startActionDownload(context: Context, param1: String) {
            val intent = Intent(context, RSSFeedDownloadService::class.java).apply {
                action = ACTION_RSS_DOWNLOAD
            }
            context.startService(intent)
        }


    }
}
