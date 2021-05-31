package com.example.myticktock

import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_base.Navigator
import com.example.shared_base.NavigatorProvider


class SingleActivity : AppCompatActivity(), NavigatorProvider {

    private lateinit var navigator: NavigatorImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)

        navigator = NavigatorImpl(this)

        UpdateManager.updateReady = {
            showDialogForCompleteUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        UpdateManager.checkForUpdate(this)
    }

    override fun onPause() {
        super.onPause()
        UpdateManager.unRegisterUpdateListener()
    }

    override fun provideNavigator(): Navigator = navigator

    private fun showDialogForCompleteUpdate() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Update is available!")
        alertDialog.setButton(BUTTON_POSITIVE, "Install") { _, _ -> UpdateManager.completeUpdate() }
        alertDialog.setButton(BUTTON_NEGATIVE, "Cancel") { _, _ -> alertDialog.hide() }
        alertDialog.show()
    }
}