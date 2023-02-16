package com.cp.dialog.api

import android.content.DialogInterface

/**
 * Created by zsf on 2023/2/16 17:00
 * ******************************************
 * *
 * ******************************************
 */
open interface OnDismissListener {
    /**
     * This method will be invoked when the dialog is dismissed.
     *
     * @param dialog the dialog that was dismissed will be passed into the
     * method
     */
    fun onDismiss(dialog: DialogInterface?)
}