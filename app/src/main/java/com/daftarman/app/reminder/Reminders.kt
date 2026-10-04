package com.daftarman.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.daftarman.app.MainActivity
import com.daftarman.app.R
import com.daftarman.app.data.AppDatabase
import com.daftarman.app.data.Installment
import com.daftarman.app.data.Txn
import com.daftarman.app.util.Jalali
import com.daftarman.app.util.fa
import com.daftarman.app.util.faNum
import com.daftarman.app.util.nextDue
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "installment_reminders"
private const val WORK_NAME = "installment-reminders"

/** هر روز حدود ساعت ۹ صبح بررسی می‌کند کدام قسط‌ها نزدیک سررسید یا عقب‌افتاده‌اند. */
class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val items = db.installmentDao().all().first()
        val txns = db.txnDao().all().first()
        Reminders.notifyDue(applicationContext, items, txns)
        return Result.success()
    }
}

object Reminders {
    /** زمان‌بندی روزانه؛ اگر قبلاً ثبت شده باشد دست نمی‌زند. */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(9, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = Duration.between(now, next).toMillis()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "یادآور اقساط", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "یادآوری سررسید اقساط ماهانه"
                }
            )
        }
    }

    /**
     * برای هر قسطی که یادآور دارد: از چند روز قبل از سررسید تا حداکثر ۷ روز بعد از آن
     * (تا وقتی پرداختش را ثبت نکرده‌ای) هر روز یک اعلان نمایش می‌دهد.
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun notifyDue(context: Context, items: List<Installment>, txns: List<Txn>) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        ensureChannel(context)
        val today = Jalali.today()

        for (item in items) {
            if (item.reminderDays < 0) continue
            val due = nextDue(item, txns) ?: continue
            val left = due - today
            if (left > item.reminderDays || left < -7) continue

            val whenText = when {
                left < 0 -> "${(-left).toInt().fa()} روز از سررسید گذشته"
                left == 0L -> "امروز سررسید است"
                else -> "${left.toInt().fa()} روز تا سررسید"
            }
            val open = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, item.id.toInt(), open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val text = "${faNum(item.amount)} تومان — $whenText (${Jalali.format(due, short = true)})"
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("قسط ${item.title}")
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                nm.notify(1000 + item.id.toInt(), notification)
            } catch (e: SecurityException) {
                // مجوز اعلان داده نشده
            }
        }
    }
}
