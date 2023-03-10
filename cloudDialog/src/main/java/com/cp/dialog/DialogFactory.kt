package com.cp.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.cp.dialog.api.OnDismissListener
import com.cp.dialog.tools.AnimatorEnum
import com.cp.dialog.tools.Applications
import com.cp.dialog.tools.MyLifecycleActImp
import kotlinx.android.synthetic.main.zxy_alert_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Created by zxy on 2019/8/24-10:13
 * Class functions
 * ******************************************
 * * 建造者模式
 * * 自定义万能布局AletDialog
 * ******************************************
 */
internal var listDialog = mutableListOf<DialogFactory>()

class DialogFactory private constructor() {

    var layoutView: View? = null                           //Dialog的布局文件
    private var cancelable: Boolean = true                          //是否可以取消  true可以
    var dialog: MyDialog? = null                        // AlertDilaog
    var listView: MutableList<Int>? = null
    var transparency: Float = 0.5f                              // 透明度
    var fullScreen: Boolean = false
    var touchOutside: Boolean = false                     //是否可以触摸外部
    var onDispatchTouchEvent: OnDispatchTouchEvent? = null
    lateinit var mContext: ComponentActivity


    companion object {
        /**
         * 必须是ComponentActivity 子类
         * 可以是AppCompatActivity、FragmentActivity等
         */
        @JvmStatic
        fun build(mContext: ComponentActivity): Builder {
            return Builder(mContext)
        }

        /**
         * 清空dialog
         */
        fun clearAllDialog() {
            for (dialogFactory in listDialog) {
                dialogFactory.dismiss()
            }
        }
    }

    interface OnDispatchTouchEvent {
        fun dispatchTouchEvent(ev: MotionEvent)
    }

    class MyDialog : Dialog {
        var dialogFactory: DialogFactory

        constructor(context: Context, themeResId: Int, dialogFactory: DialogFactory) : super(
            context, themeResId
        ) {
            this.dialogFactory = dialogFactory
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            dialogFactory.onDispatchTouchEvent.let {
                dialogFactory.onDispatchTouchEvent?.dispatchTouchEvent(ev)
            }
            fullScreenShow()
            return super.dispatchTouchEvent(ev)
        }

        override fun show() {
            if (dialogFactory.fullScreen)
                this.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
            super.show()
            fullScreenShow()
        }

        fun fullScreenShow() {
            if (dialogFactory.fullScreen)
                dialogFactory.bottomNavInVisible()
        }
    }

    class Builder {
        var mContext: ComponentActivity
        var dialogFactory: DialogFactory
        var animator: Int? = null
        var title: String? = null
        var content: String? = null
        var editTextId: Int? = null
        var isDismiss = false
        var timerLuanch: Job? = null
        lateinit var myLocationListener: MyLifecycleActImp

        var onDismissListener: OnDismissListener? = null
        var onCancelListener: DialogInterface.OnCancelListener? = null


        constructor(context: ComponentActivity) {
            dialogFactory = DialogFactory()
            dialogFactory.mContext = context
            mContext = context
            onLifecycleListener()
        }

        private fun onLifecycleListener() {
            myLocationListener =
                MyLifecycleActImp(mContext, object : MyLifecycleActImp.LifecycleListener {
                    override fun onResult() {
                        dialogFactory.dismiss()
                    }
                })
            mContext.lifecycle.addObserver(myLocationListener)
        }

        /**
         * 设置布局
         * @param layoutView View
         */
        fun setView(layoutId: Int): Builder {
            dialogFactory.layoutView = LayoutInflater.from(mContext).inflate(layoutId, null)
            return this
        }

        /**
         * 设置布局
         * @param layoutView View
         */
        fun setView(layoutView: View): Builder {
            dialogFactory.layoutView = layoutView
            return this
        }

        fun setFullScreen(fullScreen: Boolean): Builder {
            dialogFactory.fullScreen = fullScreen
            return this
        }

        //是否可以触摸外部
        fun setCanceledOnTouchOutside(touchOutside: Boolean): Builder {
            dialogFactory.touchOutside = touchOutside
            return this
        }

        /**
         * 扫码、全屏等监听
         */
        fun OnDispatchTouchEvent(onDispatchTouchEvent: OnDispatchTouchEvent): Builder {
            dialogFactory.onDispatchTouchEvent = onDispatchTouchEvent
            return this
        }

        /**
         * 是否弹出键盘
         * @param editTextId 光标位置
         */
        fun setEditFocus(editTextId: Int): Builder {
            this.editTextId = editTextId
            return this
        }


        /**
         * 设置点击事件
         * @param viewId IntArray
         */
        fun setOnClick(vararg viewId: Int): Builder {
            dialogFactory.listView = viewId.toTypedArray().toMutableList()
            return this
        }

        /**
         * 设置销毁的事件
         * @param viewId IntArray
         */
        fun setOnDismissListeners(listener: OnDismissListener): Builder {
            onDismissListener = listener
            return this
        }

        /**
         * 设置销毁的事件
         * @param viewId IntArray
         */
        fun setOnCancelListeners(listener: DialogInterface.OnCancelListener): Builder {
            onCancelListener = listener
            return this
        }

        /**
         * 是否可以取消  默认true可以
         * @param cancelable Boolean
         */
        fun setCancelable(cancelable: Boolean): Builder {
            dialogFactory.cancelable = cancelable
            return this
        }

        /**
         * 设置窗口透明度，默认0.5    0为全透明  1全黑
         * @param transparency Float
         * @return Builder
         */
        fun setTransparency(transparency: Float): Builder {
            dialogFactory.transparency = transparency
            return this
        }

        fun setAnimator(animator: Int): Builder {
            this.animator = animator
            return this
        }

        fun setValues(title: String, content: String): Builder {
            this.title = title
            this.content = content
            return this
        }

        fun setValues(title: Int, content: Int): Builder {
            this.title = Applications.context().resources.getString(title)
            this.content = Applications.context().resources.getString(content)
            return this
        }


        fun setTimerLifecy(
            time: Int,
            callBack: ((Int, DialogFactory) -> Unit) = { _, _ -> }
        ): Builder {
            timerLuanch = mContext.lifecycleScope.launch(Dispatchers.Main) {
                repeat(time) {
                    delay(1000)
                    if (!isDismiss)
                        callBack(time - it, dialogFactory)
                }
                dialogFactory.dismiss()
            }
            return this
        }


        /**
         * Dilaog 创建完成显示
         */
        fun show(callBack: ((DialogFactory) -> Unit) = {}): DialogFactory {
            if (dialogFactory.dialog == null) {
                OnClickListener()
            }
            callBack(dialogFactory)
            return dialogFactory
        }

        fun show(): DialogFactory {
            if (dialogFactory.dialog == null) {
                OnClickListener()
            }
            return dialogFactory
        }


        /**
         * 创建自定义布局的AlertDialog
         */
        fun OnClickListener(callBack: ((View, DialogFactory) -> Unit) = { _: View, _: DialogFactory -> }): Builder {
            callBack(callBack)
            return this
        }


        private fun callBack(callBack: ((View, DialogFactory) -> Unit) = { _: View, _: DialogFactory -> }) {
            if (!mContext.isDestroyed) {
                if (dialogFactory.dialog != null) {
                    dialogFactory.dismiss()
                }
                dialogFactory.dialog = MyDialog(
                    mContext,
                    R.style.zxy_MyDilog,
                    dialogFactory
                )
                if (dialogFactory.layoutView == null) {//自带的dialog
                    setView(R.layout.zxy_alert_dialog)
                    setOnClick(R.id.tvDialogCancel, R.id.tvDialogConfig)
                    dialogFactory.layoutView?.tvDialgTitle?.text = title
                    dialogFactory.layoutView?.tvDialgContent?.text = content
                }
                dialogFactory.dialog?.setContentView(dialogFactory.layoutView!!)
                dialogFactory.dialog?.setCancelable(dialogFactory.cancelable)
                dialogFactory.dialog?.setCanceledOnTouchOutside(dialogFactory.touchOutside)
                //设置动画
                val window = dialogFactory.dialog?.window
                val layoutParams = window?.attributes
                layoutParams?.windowAnimations = animator ?: AnimatorEnum.ZOOM.VALUE
                window?.attributes = layoutParams

                dialogFactory.dialog?.fullScreenShow()
                dialogFactory.dialog?.show()
                listDialog.add(dialogFactory)
                if (editTextId != null) {
                    dialogFactory.layoutView?.postDelayed({
                        showKeyboard(dialogFactory.layoutView?.findViewById(editTextId!!))
                    }, 100)
                }

                val lp = dialogFactory.dialog?.window!!.attributes
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialogFactory.dialog?.window!!.setDimAmount(dialogFactory.transparency)//设置黑色遮罩层的透明度
                dialogFactory.dialog?.window!!.attributes = lp

                if (dialogFactory.listView != null) {
                    for (index in dialogFactory.listView!!) {
                        dialogFactory.layoutView!!.findViewById<View>(index)
                            .setOnClickListener {
                                if (!mContext.isDestroyed) {
                                    callBack(it, dialogFactory)
                                } else {
                                    Log.e("", "当前activity已销毁")
                                }
                            }
                    }
                }

                if (onCancelListener != null) {
                    dialogFactory.dialog?.setOnCancelListener(onCancelListener)
                }

                dialogFactory.dialog?.setOnDismissListener {
                    timerLuanch?.cancel()
                    isDismiss = true
                    if (onDismissListener != null) {
                        onDismissListener!!.onDismiss(it)
                    }
                }
            } else {
                Log.e("", "当前activity已销毁")
            }
        }

        fun cancel() {
            if (dialogFactory.dialog != null)
                dialogFactory.dialog!!.cancel()
        }

        //弹出软键盘
        private fun showKeyboard(editText: EditText?) {
            //其中editText为dialog中的输入框的 EditText
            if (editText != null) {
                //设置可获得焦点
                editText.isFocusable = true
                editText.isFocusableInTouchMode = true
                //请求获得焦点
                editText.requestFocus()
                //调用系统输入法
                val inputManager: InputMethodManager =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(editText, 0)
            }
        }
    }

    fun bottomNavInVisible() {
        //隐藏虚拟按键，并且全屏
        var decorView = dialog?.window?.decorView
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            var decorView = dialog?.window?.decorView
            decorView?.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }


    fun dismiss() {
        if (dialog != null) {
            dialog!!.cancel()
            dialog = null
        }
    }


}