package com.cloud.dialog.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.cloud.dialog.R
import com.cp.dialog.TToast
import com.cp.dialog.snackbar.ZToast
import com.cp.dialog.tools.Applications
import com.cp.dialog.tools.LoadingTool
import kotlinx.android.synthetic.main.activity_toast.*

class ToastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toast)
        initView()
    }

    private fun initView() {
        btnToast.setOnClickListener {
            TToast.show("请输入正确的手机号")
        }

        btnToast1.setOnClickListener {
            TToast.show("你好我好")
        }

        btnCoustToast.setOnClickListener {
            var layoutView =
                LayoutInflater.from(Applications.context()).inflate(R.layout.toast_course, null)
            TToast.show(layoutView)
        }

        btnZToast.setOnClickListener {
            ZToast.setColorI("#000000")
            ZToast.showI(this, "网路错误")
        }


        btnLoad.setOnClickListener {
            LoadingTool.show(this,"加载中...");
        }
    }
}