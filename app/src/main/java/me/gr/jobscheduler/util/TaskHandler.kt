package me.gr.jobscheduler.util

import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat.getColor
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import me.gr.jobscheduler.MainActivity
import me.gr.jobscheduler.R
import me.gr.jobscheduler.common.MSG_COLOR_START
import me.gr.jobscheduler.common.MSG_COLOR_STOP
import me.gr.jobscheduler.common.MSG_UNCOLOR_START
import me.gr.jobscheduler.common.MSG_UNCOLOR_STOP
import java.lang.ref.WeakReference

internal class TaskHandler(activity: MainActivity) : Handler() {
    private val activityRefer = WeakReference<MainActivity>(activity)

    override fun handleMessage(msg: Message) {
        val mainActivity = activityRefer.get() ?: return
        val startTask = mainActivity.start_task
        val stopTask = mainActivity.stop_task
        when (msg.what) {
            MSG_COLOR_START -> {
                startTask.setBackgroundColor(getColor(mainActivity, R.color.start_received))
                updateParams(msg.obj, "started")
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_START), 1000)
            }
            MSG_COLOR_STOP -> {
                stopTask.setBackgroundColor(getColor(mainActivity, R.color.stop_received))
                updateParams(msg.obj, "stopped")
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_STOP), 1000)
            }
            MSG_UNCOLOR_START -> {
                clearColorAndParams(startTask)
            }
            MSG_UNCOLOR_STOP -> {
                clearColorAndParams(stopTask)
            }
        }
    }

    private fun updateParams(jobId: Any, action: String) {
        val mainActivity = activityRefer.get() ?: return
        mainActivity.task_params.text = mainActivity.getString(R.string.job_status, jobId, action)
    }

    private fun clearColorAndParams(view: View) {
        val mainActivity = activityRefer.get() ?: return
        view.setBackgroundColor(getColor(mainActivity, R.color.none_received))
        mainActivity.task_params.text = ""
    }
}