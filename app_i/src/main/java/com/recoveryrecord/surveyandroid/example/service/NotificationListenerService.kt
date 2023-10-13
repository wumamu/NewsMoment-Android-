package com.recoveryrecord.surveyandroid.example.service

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.recoveryrecord.surveyandroid.example.config.Constants
import com.recoveryrecord.surveyandroid.example.config.Constants.NEWS_ID_KEY
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_DEVICE_ID
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_NOTI_TIME
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_PACKAGE_ID
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_SOURCE
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_TEXT
import com.recoveryrecord.surveyandroid.example.config.Constants.NOTIFICATION_BAR_NEWS_TITLE
import com.recoveryrecord.surveyandroid.example.config.Constants.NO_VALUE
import com.recoveryrecord.surveyandroid.example.config.Constants.PUSH_NEWS_COLLECTION
import com.recoveryrecord.surveyandroid.example.config.Constants.PUSH_NEWS_RECEIEVE_TIME
import com.recoveryrecord.surveyandroid.example.config.Constants.PUSH_NEWS_REMOVE_TIME
import com.recoveryrecord.surveyandroid.example.config.Constants.PUSH_NEWS_REMOVE_TYPE
import com.recoveryrecord.surveyandroid.example.util.insertRemote
import com.recoveryrecord.surveyandroid.example.util.updateRemote
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var db: FirebaseFirestore

    companion object {
        private val notificationRemoveReason = lazy {
            mapOf(
                1 to "REASON_CLICK",
                2 to "REASON_CANCEL",
                3 to "REASON_CANCEL_ALL",
                4 to "REASON_ERROR",
                5 to "REASON_PACKAGE_CHANGED",
                6 to "REASON_USER_STOPPED",
                7 to "REASON_PACKAGE_BANNED",
                8 to "REASON_APP_CANCEL",
                9 to "REASON_APP_CANCEL_ALL",
                10 to "REASON_LISTENER_CANCEL",
                11 to "REASON_LISTENER_CANCEL_ALL",
                12 to "REASON_GROUP_SUMMARY_CANCELED",
                13 to "REASON_GROUP_OPTIMIZATION",
                14 to "REASON_PACKAGE_SUSPENDED",
                15 to "REASON_PROFILE_TURNED_OFF",
                16 to "REASON_UNAUTOBUNDLED",
                17 to "REASON_CHANNEL_BANNED",
                18 to "REASON_SNOOZED",
                19 to "REASON_TIMEOUT",
                20 to "REASON_CHANNEL_REMOVED",
                21 to "REASON_CLEAR_DATA"
            )
        }
        private val notificationTarget = lazy {
            setOf(
                Constants.CHINA_TIMES_PACKAGE_NAME,
                Constants.CNA_PACKAGE_NAME,
                Constants.UDN_PACKAGE_NAME,
                Constants.LTN_PACKAGE_NAME,
                Constants.ETTODAY_PACKAGE_NAME,
                Constants.CTS_PACKAGE_NAME,
                Constants.EBC_PACKAGE_NAME,
                Constants.STORM_PACKAGE_NAME,
                Constants.SETS_PACKAGE_NAME
            )
        }
        private val selectedDevice = lazy {
            setOf("9601fcc5ef9fc3be", "0ceac22db4d7789f", "cd96440867380664", "d254e371a0970955")
        }
        var deviceId: String = ""
        const val COMPARE_COLLECTION = "compare"
        fun getPushNewsDocId(curNewsId: String): String {
            return "$deviceId$curNewsId"
        }

        fun getCompareDocId(): String {
            return "$deviceId${Timestamp.now().nanoseconds}"
        }
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("com.recoveryrecord.surveyandroid.example.NOTIFICATION_LISTENER_SERVICE_EXAMPLE")
        deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        val isTarget = notificationTarget.value.contains(sbn.packageName)
        // cancel notification
        if (isTarget) {
            cancelNotification(sbn.key)
            Timber.d(sbn.packageName + "being canceled")
        }
        // my notification
        if (sbn.packageName == this.packageName) {
            val notificationNewId = sbn.notification.extras?.getString(NEWS_ID_KEY)
            notificationNewId?.let { newsId ->
                val updateData = hashMapOf<String, Any>(
                    PUSH_NEWS_RECEIEVE_TIME to Timestamp.now(),
                )
                CoroutineScope(Dispatchers.IO).launch {
                    updateRemote(
                        db.collection(PUSH_NEWS_COLLECTION).document(getPushNewsDocId(newsId)),
                        updateData
                    ) {
                        Timber.d("onNotificationReceived update success")
                    }
                }
            }
        } else if (isTarget) {
            Timber.d(
                "onNotificationReceived is target ${sbn.packageName}" +
                        " ${sbn.notification.extras?.getString("android.title")}" +
                        " ${sbn.notification.extras?.getCharSequence("android.text")}"
            )
            if (deviceId in selectedDevice.value) {
                sbn.notification.extras?.apply {
                    val updateData = hashMapOf<String, Any>(
                        NOTIFICATION_BAR_NEWS_SOURCE to sbn.packageName,
                        NOTIFICATION_BAR_NEWS_NOTI_TIME to Timestamp.now(),
                        NOTIFICATION_BAR_NEWS_PACKAGE_ID to sbn.key,
                        NOTIFICATION_BAR_NEWS_DEVICE_ID to deviceId,
                        NOTIFICATION_BAR_NEWS_TITLE to (this.getString("android.title")
                            ?: NO_VALUE),
                        NOTIFICATION_BAR_NEWS_TEXT to (this.getCharSequence("android.text")
                            ?: NO_VALUE),
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        insertRemote(
                            db.collection(COMPARE_COLLECTION).document(getCompareDocId()),
                            updateData
                        ) {
                            Timber.d("Compare new update!")
                        }
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {
        val removeReason = notificationRemoveReason.value[reason] ?: "UNKNOWN"
        Timber.d("onNotificationRemoved $removeReason ${sbn.packageName}")

        if (sbn.packageName == this.packageName) {
            val notificationNewId = sbn.notification.extras?.getString(NEWS_ID_KEY)
            notificationNewId?.let { newsId ->
                val updateData = hashMapOf<String, Any>(
                    PUSH_NEWS_REMOVE_TIME to Timestamp.now(),
                    PUSH_NEWS_REMOVE_TYPE to removeReason
                )
                CoroutineScope(Dispatchers.IO).launch {
                    updateRemote(
                        db.collection(PUSH_NEWS_COLLECTION).document(getPushNewsDocId(newsId)),
                        updateData
                    ) {
                        Timber.d("onNotificationRemoved update success")
                    }
                }
            }
        }
    }
}