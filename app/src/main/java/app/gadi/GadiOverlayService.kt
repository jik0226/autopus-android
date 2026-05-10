package app.gadi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class GadiOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayParams: WindowManager.LayoutParams
    private var overlayView: View? = null
    private var chatPanel: LinearLayout? = null
    private var chatInput: EditText? = null
    private var bubbleText: TextView? = null
    private var isChatOpen = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(NOTIFICATION_ID, createNotification())

        if (Settings.canDrawOverlays(this)) {
            showOverlay()
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlayView == null && Settings.canDrawOverlays(this)) {
            showOverlay()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val collapsedSize = dpToPx(112)
        val root = FrameLayout(this).apply {
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            isFocusableInTouchMode = true
            setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_OUTSIDE && isChatOpen) {
                    setChatOpen(false)
                    true
                } else {
                    false
                }
            }
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && isChatOpen) {
                    setChatOpen(false)
                    true
                } else {
                    false
                }
            }
        }
        val image = ImageView(this).apply {
            setImageResource(R.drawable.gadi_idle)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val imageParams = FrameLayout.LayoutParams(collapsedSize, collapsedSize).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }
        root.addView(image, imageParams)

        val panel = createChatPanel().apply {
            visibility = View.GONE
        }
        val panelParams = FrameLayout.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = collapsedSize + dpToPx(8)
        }
        root.addView(panel, panelParams)
        chatPanel = panel

        overlayParams = WindowManager.LayoutParams(
            collapsedSize,
            collapsedSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(24)
            y = dpToPx(96)
        }

        image.setOnTouchListener(DragTouchListener(overlayParams))
        windowManager.addView(root, overlayParams)
        overlayView = root
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            windowManager.removeView(view)
            overlayView = null
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Gadi overlay",
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(R.drawable.ic_gadi_notification)
            .setContentTitle("Gadi is floating")
            .setContentText("Tap Gadi to supervise mobile automation.")
            .setOngoing(true)
            .build()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun createChatPanel(): LinearLayout {
        val panelPadding = dpToPx(12)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(panelPadding, panelPadding, panelPadding, panelPadding)
            background = roundedBackground(Color.WHITE, 12)
            elevation = dpToPx(6).toFloat()

            bubbleText = TextView(context).apply {
                text = "무엇을 도와줄까요?"
                textSize = 15f
                setTextColor(Color.rgb(23, 32, 51))
            }
            addView(
                bubbleText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dpToPx(10), 0, 0)
            }
            chatInput = EditText(context).apply {
                hint = "메시지를 입력하세요"
                setSingleLine(true)
                textSize = 14f
            }
            row.addView(
                chatInput,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
            )
            row.addView(
                Button(context).apply {
                    text = "Send"
                    setOnClickListener {
                        bubbleText?.text = "LLM 연결은 Week 3에서 시작해요."
                        chatInput?.text?.clear()
                    }
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
    }

    private fun setChatOpen(open: Boolean) {
        if (isChatOpen == open) return
        isChatOpen = open
        chatPanel?.visibility = if (open) View.VISIBLE else View.GONE

        overlayParams.width = if (open) dpToPx(320) else dpToPx(112)
        overlayParams.height = if (open) WindowManager.LayoutParams.WRAP_CONTENT else dpToPx(112)
        overlayParams.flags = if (open) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        overlayView?.let { view ->
            windowManager.updateViewLayout(view, overlayParams)
            if (open) {
                view.requestFocus()
                chatInput?.requestFocus()
                showKeyboard()
            } else {
                hideKeyboard(view)
            }
        }
    }

    private fun showKeyboard() {
        val input = chatInput ?: return
        input.post {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun roundedBackground(color: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dpToPx(radiusDp).toFloat()
        }

    private inner class DragTouchListener(
        private val params: WindowManager.LayoutParams,
    ) : View.OnTouchListener {
        private var startX = 0
        private var startY = 0
        private var touchStartX = 0f
        private var touchStartY = 0f
        private var moved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    moved = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - touchStartX
                    val deltaY = event.rawY - touchStartY
                    if (!moved && (kotlin.math.abs(deltaX) > dpToPx(6) || kotlin.math.abs(deltaY) > dpToPx(6))) {
                        moved = true
                    }
                    if (moved) {
                        params.x = startX + deltaX.toInt()
                        params.y = startY + deltaY.toInt()
                        overlayView?.let { windowManager.updateViewLayout(it, params) }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        setChatOpen(!isChatOpen)
                    }
                    return true
                }
            }
            return true
        }
    }

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "gadi_overlay"
        const val NOTIFICATION_ID = 1001
    }
}
