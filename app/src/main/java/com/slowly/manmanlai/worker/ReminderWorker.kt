package com.slowly.manmanlai.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.slowly.manmanlai.MainActivity
import com.slowly.manmanlai.AppDatabase
import com.slowly.manmanlai.TaskStatus
import com.slowly.manmanlai.R
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val title = inputData.getString(KEY_TITLE) ?: "\u4eca\u5929\u7684\u4e00\u5c0f\u6b65"
        val taskId = inputData.getLong(KEY_TASK_ID, 0L)
        val db = AppDatabase.create(applicationContext)
        val task = db.taskDao().taskById(taskId)
        db.close()
        if (task?.status != TaskStatus.TODO) return Result.success()
        val label = inputData.getString(KEY_LABEL) ?: "#$taskId"
        val repeatMinutes = inputData.getInt(KEY_REPEAT_MINUTES, 0).takeIf { it > 0 }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("\u6162\u6162\u6765 $label")
            .setContentText("$title\uff0c\u53ef\u4ee5\u5148\u505a 5 \u5206\u949f\u3002")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$label  $title\n\u53ef\u4ee5\u5148\u505a 5 \u5206\u949f\u3002\u4e0d\u662f\u8981\u4e00\u4e0b\u5b50\u505a\u5b8c\uff0c\u53ea\u662f\u5f00\u59cb\u4e00\u4e0b\u3002",
                ),
            )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(taskId.toInt(), notification)
        if (repeatMinutes != null) {
            NotificationHelper.schedule(
                applicationContext,
                taskId,
                title,
                System.currentTimeMillis() + repeatMinutes * 60 * 1000L,
                repeatMinutes,
                label,
            )
        }
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TITLE = "title"
        const val KEY_LABEL = "label"
        const val KEY_REPEAT_MINUTES = "repeat_minutes"
    }
}

object NotificationHelper {
    const val CHANNEL_ID = "gentle_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "\u6e29\u548c\u63d0\u9192",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "\u7528\u4e0d\u50ac\u4fc3\u7684\u65b9\u5f0f\u63d0\u9192\u4f60\u56de\u5230\u8ba1\u5212\u3002"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, taskId: Long, title: String, remindAt: Long?, repeatMinutes: Int? = null, label: String = "#$taskId") {
        if (remindAt == null) return
        val delay = (remindAt - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong(ReminderWorker.KEY_TASK_ID, taskId)
                    .putString(ReminderWorker.KEY_TITLE, title)
                    .putString(ReminderWorker.KEY_LABEL, label)
                    .putInt(ReminderWorker.KEY_REPEAT_MINUTES, repeatMinutes ?: 0)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(taskId), androidx.work.ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }

    private fun workName(taskId: Long): String = "task-reminder-$taskId"
}
