package com.cp.dialog.tools

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by zsf on 2022/9/5 12:36
 * ******************************************
 * *
 * ******************************************
 */
class MyLifecycleActImp : LifecycleObserver {
    var lifecycleListener: LifecycleListener
    var mContext: ComponentActivity
    constructor(mContext:ComponentActivity,lifecycleListener: LifecycleListener){
        this.mContext = mContext
        this.lifecycleListener = lifecycleListener
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDistroy() {
        lifecycleListener.onResult()
        mContext.lifecycle.removeObserver(this)
    }

    interface LifecycleListener{
        fun onResult()
    }
}