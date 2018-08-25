package com.gmits.watchfacetouchsample


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.format.DateUtils
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*


class WatchfaceSample : CanvasWatchFaceService() {

    internal val MSG_UPDATE_TIME = 1
    lateinit var tvTime: TextView
    lateinit var tvAmPm: TextView
    var handler: Handler? = null
    lateinit var llMiddleWear: LinearLayout

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine : CanvasWatchFaceService.Engine() {

        private var isZoneReceiverRegistered = false
        private var layoutView: View? = null
        private var seconds: Int = 0
        internal var time12flag: Boolean = false

        private val updateTimeHandler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                MSG_UPDATE_TIME -> {
                    updateDigits()
                    updateTimer()
                }
            }
            false
        })

        private val timeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateDigits()
            }
        }


        override fun onTapCommand(@TapType tapType: Int, x: Int, y: Int, eventTime: Long) {

            when (tapType) {

                WatchFaceService.TAP_TYPE_TAP -> {

                }

                WatchFaceService.TAP_TYPE_TOUCH -> {

                    if (x >= llMiddleWear.x
                            && y >= llMiddleWear.y
                            && x <= (llMiddleWear.x + llMiddleWear.width)
                            && y <= (llMiddleWear.y + llMiddleWear.height)) {

                        time12flag = !time12flag
                        invalidate()

                    }
                }

                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {

                }

                else -> super.onTapCommand(tapType, x, y, eventTime)
            }
        }


        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            layoutView = inflater.inflate(R.layout.watchface_sample, null)

            tvTime = (layoutView!!.findViewById<View>(R.id.tvTime) as TextView)
            tvAmPm = (layoutView!!.findViewById<View>(R.id.tvAmPm) as TextView)
            llMiddleWear = (layoutView!!.findViewById<View>(R.id.llMiddleWear) as LinearLayout)

        }

        override fun onDestroy() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                updateDigits()
                registerZoneReceiver()

            } else {
                unregisterZoneReceiver()
            }
            updateTimer()
        }


        private fun registerZoneReceiver() {
            if (isZoneReceiverRegistered) {
                return
            }
            isZoneReceiverRegistered = true
            this@WatchfaceSample.registerReceiver(timeZoneReceiver,
                    IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
        }

        private fun unregisterZoneReceiver() {
            if (!isZoneReceiverRegistered) {
                return
            }
            this@WatchfaceSample.unregisterReceiver(timeZoneReceiver)
            isZoneReceiverRegistered = false
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            val gravity = if (!insets.isRound) Gravity.START or Gravity.TOP else Gravity.CENTER
            setWatchFaceStyle(WatchFaceStyle
                    .Builder(this@WatchfaceSample)
                    .setStatusBarGravity(gravity)
                    .setAcceptsTapEvents(true)
                    .setAccentColor(-0xad9302)
                    .build())

            val displaySize = Point()
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                    .getSize(displaySize)

            if (layoutView is LinearLayout) {
                val height = (displaySize.y * 0.28).toInt()
                val width = (height * 0.62).toInt()

            }
            val specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY)
            val specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY)
            layoutView!!.measure(specW, specH)
            layoutView!!.layout(0, 0, layoutView!!.measuredWidth, layoutView!!.measuredHeight)
        }

        override fun onTimeTick() {
            super.onTimeTick()
            updateDigits()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)

            if (inAmbientMode) {
            } else {
                updateDigits()
            }
            invalidate()
            updateTimer()
        }


        override fun onDraw(canvas: Canvas?, bounds: Rect?) {
            layoutView!!.draw(canvas)
        }


        private fun updateTimer() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            val delayMs = DateUtils.SECOND_IN_MILLIS - System.currentTimeMillis() % DateUtils.SECOND_IN_MILLIS
            updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
        }


        private fun getTimeNow(is12F: Boolean): String {
            var finalTime = ""
            try {
                val date = Date()
                val m24HourHour = SimpleDateFormat("HH", Locale.getDefault())
                val m24HourMinute = SimpleDateFormat("mm", Locale.getDefault())
                val m12HourHour = SimpleDateFormat("hh", Locale.getDefault())
                val m12HourMinute = SimpleDateFormat("mm", Locale.getDefault())
                val mAmPm = SimpleDateFormat("a", Locale.getDefault())
                finalTime = if (is12F)
                    m12HourHour.format(date) + "__" + m12HourMinute.format(date) + "__" + mAmPm.format(date)
                else
                    m24HourHour.format(date) + "__" + m24HourMinute.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return finalTime
        }


        private fun updateDigits() {

            val now = Calendar.getInstance()
            var time: String = getTimeNow(time12flag)

            seconds = now.get(Calendar.SECOND)
            tvTime.setText(time.split("__")[0] + ":" + time.split("__")[1])

            var mAmPm: String

            if (time12flag) {
                tvAmPm.visibility = View.VISIBLE
                mAmPm = time.split("__")[2]
                tvAmPm.text = mAmPm
            } else {
                tvAmPm.visibility = View.GONE
            }

        }
    }
}