package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.ufpe.cin.android.podcast.services.RSSFeedDownloadService

class DownloadWorker (appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val intent = Intent(applicationContext, RSSFeedDownloadService::class.java)
        applicationContext.startService(intent)

        return Result.success()
    }
}