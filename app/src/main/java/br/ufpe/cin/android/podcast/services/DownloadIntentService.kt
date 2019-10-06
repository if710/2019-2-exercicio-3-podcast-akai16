package br.ufpe.cin.android.podcast.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.ufpe.cin.android.podcast.models.PodcastDB
import br.ufpe.cin.android.podcast.consts.Consts
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val ACTION_DOWNLOAD = "br.ufpe.cin.android.podcast.action.download"

class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_DOWNLOAD -> {
                val feedItemId = intent.getIntExtra(Consts.EXTRA_FEED_ID, -1)
                val feedItemPosition = intent.getIntExtra(Consts.EXTRA_FEED_ITEM_POSITION, -1)
                handleDownload(feedItemId, feedItemPosition)
            }
        }
    }

    private fun handleDownload(feedItemId: Int, feeItemPosition: Int) {

        val podcastDB = PodcastDB.getDatabase(this@DownloadIntentService)

        val feedItem = podcastDB.itemFeedDAO().getFeedItemByID(feedItemId)

        val fileName = feedItem.downloadLink.split('/').last().substringBefore('?')

        val httpCon = (URL(feedItem.downloadLink).openConnection() as HttpURLConnection)
        val mp3File = File(this@DownloadIntentService.getExternalFilesDir(null), fileName)

        val mp3Stream = mp3File.outputStream()

        try {
            Toast.makeText(this@DownloadIntentService.applicationContext, "Iniciando download...", Toast.LENGTH_SHORT).show()

            // Baixando arquivo mp3
            val inStream = BufferedInputStream(httpCon.inputStream)

            val data = ByteArray(1024)
            var bufferLength = 0

            do {
                bufferLength = inStream.read(data)
                mp3Stream.write(data)
            } while (bufferLength > 0)

            Log.d(Consts.DEBUG_TAG, "Download concluido")

            Toast.makeText(this@DownloadIntentService.applicationContext, "Download concluido", Toast.LENGTH_SHORT).show()

            // Salvando no Banco de Dados
            val itemFeed = podcastDB.itemFeedDAO().getFeedItemByID(feedItemId)

            itemFeed.mp3Path = mp3File.path
            Log.d(Consts.DEBUG_TAG, "Arquivo encontrado no DB")

            podcastDB.itemFeedDAO().updateFeedItem(itemFeed)
            Log.d(Consts.DEBUG_TAG, "Arquivo salvo no DB")

            val intent = Intent(Consts.BROADCAST_DOWNLOAD_FINISHED)
            intent.putExtra(Consts.EXTRA_FEED_ID, feedItemId)
            intent.putExtra(Consts.EXTRA_FEED_ITEM_POSITION, feeItemPosition)

            LocalBroadcastManager.getInstance(this@DownloadIntentService).sendBroadcast(intent)
            Log.d(Consts.DEBUG_TAG, "Enviando BROADCAST_DOWNLOAD_FINISHED")

        } catch (exception: IOException) {
            Log.d(Consts.DEBUG_TAG, "Não foi possível baixar o arquivo")
        } finally {
            httpCon.disconnect()
            mp3Stream.close()
        }


    }

    companion object {
        // Metodo "estatico" para iniciar um novo DownloadIntentService
        @JvmStatic
        fun startActionDonwload(context: Context, feedItemId: Int, itemPosition: Int) {
            val intent = Intent(context, DownloadIntentService::class.java).apply {
                action = ACTION_DOWNLOAD
                putExtra(Consts.EXTRA_FEED_ID, feedItemId)
                putExtra(Consts.EXTRA_FEED_ITEM_POSITION, itemPosition)
            }
            context.startService(intent)
        }


    }
}
