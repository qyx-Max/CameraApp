package com.example.cameraapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // UI组件
    private lateinit var etServerIp: EditText
    private lateinit var btnTestConnection: Button
    private lateinit var btnCapture: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar

    // 网络管理器
    private lateinit var networkManager: NetworkManager

    // 状态跟踪
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化UI组件
        initViews()

        // 初始化网络管理器
        networkManager = NetworkManager()

        // 设置按钮点击监听器
        setupClickListeners()

        // 初始状态
        updateUIState()
    }

    /**
     * 初始化所有视图组件
     */
    private fun initViews() {
        etServerIp = findViewById(R.id.etServerIp)
        btnTestConnection = findViewById(R.id.btnTestConnection)
        btnCapture = findViewById(R.id.btnCapture)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * 设置按钮点击事件
     */
    private fun setupClickListeners() {
        // 测试连接按钮
        btnTestConnection.setOnClickListener {
            testConnection()
        }

        // 拍照按钮
        btnCapture.setOnClickListener {
            capturePhoto()
        }

        // IP地址输入框的焦点变化监听
        etServerIp.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && etServerIp.text.toString() == "等待A同学提供IP") {
                // 清空提示文本
                etServerIp.text.clear()
            }
        }
    }

    /**
     * 测试连接功能
     */
    private fun testConnection() {
        // 获取IP地址
        val ip = etServerIp.text.toString().trim()

        if (ip.isEmpty()) {
            Toast.makeText(this, "请输入服务器IP地址", Toast.LENGTH_SHORT).show()
            return
        }

        // 保存IP到网络管理器
        networkManager.serverIp = ip

        // 更新UI状态
        tvStatus.text = "🔄 正在测试连接...\n目标服务器: $ip"
        progressBar.visibility = View.VISIBLE
        btnTestConnection.isEnabled = false

        // 调用网络管理器测试连接
        networkManager.testConnection { success, message ->
            runOnUiThread {
                // 更新连接状态
                isConnected = success

                // 恢复UI状态
                progressBar.visibility = View.GONE
                btnTestConnection.isEnabled = true

                // 显示结果
                tvStatus.text = message

                // 显示Toast提示
                val toastMessage = if (success) "连接成功！" else "连接失败"
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()

                // 更新UI状态
                updateUIState()
            }
        }
    }

    /**
     * 拍照功能
     */
    private fun capturePhoto() {
        // 检查是否已连接
        if (!isConnected) {
            Toast.makeText(this, "请先测试连接服务器", Toast.LENGTH_SHORT).show()
            return
        }

        // 更新UI状态
        tvStatus.text = "📸 正在拍照...\n请保持相机稳定"
        progressBar.visibility = View.VISIBLE
        btnCapture.isEnabled = false

        // 调用网络管理器拍照
        networkManager.capturePhoto { success, message ->
            runOnUiThread {
                // 恢复UI状态
                progressBar.visibility = View.GONE
                btnCapture.isEnabled = true

                // 显示结果
                tvStatus.text = message

                // 显示Toast提示
                val toastMessage = if (success) "拍照成功！" else "拍照失败"
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 根据连接状态更新UI
     */
    private fun updateUIState() {
        // 拍照按钮状态
        btnCapture.isEnabled = isConnected

        // 拍照按钮颜色
        if (isConnected) {
            btnCapture.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, theme))
        } else {
            btnCapture.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
        }
    }

    /**
     * 保存状态（可选，用于横竖屏切换）
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isConnected", isConnected)
        outState.putString("serverIp", etServerIp.text.toString())
    }

    /**
     * 恢复状态（可选）
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isConnected = savedInstanceState.getBoolean("isConnected", false)
        val savedIp = savedInstanceState.getString("serverIp", "")
        if (savedIp.isNotEmpty()) {
            etServerIp.setText(savedIp)
        }
        updateUIState()
    }
}
