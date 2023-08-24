package com.example.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.work.*
import com.example.common.AndroidXDataStore
import com.example.common.toReleaseNotes
import com.programmersbox.helpfulutils.NotificationDslBuilder
import com.programmersbox.helpfulutils.createNotificationChannel
import com.programmersbox.helpfulutils.notificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ktsoup.KtSoupParser
import ktsoup.parseRemote
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class AndroidXReleaseNotesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationContext.createNotificationChannel("androidxchecker")

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "androidxChecker",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AndroidXChecker>(1, TimeUnit.DAYS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )

    }
}

class AndroidXChecker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    @SuppressLint("SimpleDateFormat")
    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            val last = withContext(Dispatchers.Default) { AndroidXDataStore.lastUpdate.first() }

            val info = KtSoupParser.parseRemote("https://developer.android.com/feeds/androidx-release-notes.xml")

            val latest = info.querySelector("updated")
                ?.textContent()
                ?.let(format::parse)
                ?.time ?: 0L

            if (latest > last) {
                val latestInfo = info.toReleaseNotes().firstOrNull()

                val n = NotificationDslBuilder.builder(
                    applicationContext,
                    "androidxchecker",
                    android.R.mipmap.sym_def_app_icon
                ) {
                    title = "New AndroidX Update!"
                    latestInfo?.date?.let { subText = it }
                }

                applicationContext.notificationManager.notify(12, n)

                AndroidXDataStore.updateLastUpdate(latest)
            }

            return@withContext Result.success()
        }
        return Result.success()
    }
}