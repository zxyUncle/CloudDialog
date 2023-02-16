package com.cloud.dialog.manager

import android.view.MotionEvent
import androidx.activity.ComponentActivity
import com.cp.dialog.R
import com.cp.dialog.DialogFactory

object DialogManager {
    /**
     *
     */
    fun showCustom(mContext: ComponentActivity) {
        DialogFactory.build(mContext)
            .setValues("Title", "Content")
            .setFullScreen(true)
            .OnDispatchTouchEvent(object : DialogFactory.OnDispatchTouchEvent {
                override fun dispatchTouchEvent(ev: MotionEvent) {

                }
            })
            .OnClickListener { view, alertDialogUtils ->
                when (view.id) {
                    R.id.tvDialogConfig -> {

                    }
                    R.id.tvDialogCancel -> {
                    }
                }
                alertDialogUtils.dismiss()
            }.show()
    }
}