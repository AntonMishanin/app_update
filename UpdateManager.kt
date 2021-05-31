package com.example.myticktock

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability


object UpdateManager {

    var updateReady: (() -> Unit)? = null

    private var appUpdateManager: AppUpdateManager? = null
    const val UPDATE_REQUEST_CODE = 8
    private const val TAG = "UpdateManager"

    private val installStateUpdatedListener =
        InstallStateUpdatedListener { state: InstallState ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded()
            }
        }

    fun checkForUpdate(activity: Activity) {
        Log.d(TAG, "checkForUpdate")
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        registerUpdateListener()
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            checkUpdateInfo(appUpdateInfo, activity)
        }
    }

    private fun checkUpdateInfo(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        Log.d(TAG, "checkUpdateInfo")
        if (isUpdateAlreadyDownloaded(appUpdateInfo)) {
            updateReady?.invoke()
        } else if (isUpdateAvailable(appUpdateInfo)) {
            prepareForStartUpdate(appUpdateInfo, activity)
        }
    }

    private fun isUpdateAlreadyDownloaded(appUpdateInfo: AppUpdateInfo): Boolean =
        appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED

    private fun isUpdateAvailable(appUpdateInfo: AppUpdateInfo): Boolean =
        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

    private fun prepareForStartUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        Log.d(TAG, "prepareForStartUpdate")
        if (!activity.isFinishing) {
            try {
                Log.d(TAG, "try startUpdate")
                startUpdate(appUpdateInfo, activity)
            } catch (e: SendIntentException) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "SendError: $e")
                }
            }
        }
    }

    //You can handle event - user canceled - in onActivityResult
    @Throws(SendIntentException::class)
    private fun startUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) =
        appUpdateManager?.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            activity,
            UPDATE_REQUEST_CODE
        )

    private fun registerUpdateListener() =
        appUpdateManager?.registerListener(installStateUpdatedListener)

    fun unRegisterUpdateListener() =
        appUpdateManager?.unregisterListener(installStateUpdatedListener)

    fun completeUpdate() = appUpdateManager?.completeUpdate()

    private fun onUpdateDownloaded() {
        unRegisterUpdateListener()
        Log.d(TAG, "onUpdateDownloaded()")
        if (updateReady == null) {
            appUpdateManager?.completeUpdate()
        } else {
            updateReady?.invoke()
        }
    }
}