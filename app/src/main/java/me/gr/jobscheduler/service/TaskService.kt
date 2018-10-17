package me.gr.jobscheduler.service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import me.gr.jobscheduler.common.MESSENGER_INTENT_KEY
import me.gr.jobscheduler.common.MSG_COLOR_START
import me.gr.jobscheduler.common.MSG_COLOR_STOP
import me.gr.jobscheduler.common.WORK_DURATION_KEY
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class TaskService : JobService(), AnkoLogger {
    private var messenger: Messenger? = null
    private val handler = Handler()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        messenger = intent?.getParcelableExtra(MESSENGER_INTENT_KEY)
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val jobId = params.jobId
        sendMessage(MSG_COLOR_START, jobId)

        val duration = params.extras.getLong(WORK_DURATION_KEY)
        handler.postDelayed({
            sendMessage(MSG_COLOR_STOP, jobId)
            jobFinished(params, false)
        }, duration)

        info("on start job: $jobId")
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        val jobId = params.jobId
        sendMessage(MSG_COLOR_STOP, jobId)
        info("on stop job: $jobId")
        return false
    }

    private fun sendMessage(msgId: Int, params: Any) {
        if (messenger == null) {
            debug("Service is bound, not started. There's no callback to send a message to.")
            return
        }

        val message = Message.obtain().apply {
            what = msgId
            obj = params
        }
        try {
            messenger?.send(message)
        } catch (e: RemoteException) {
            error("Error passing service object back to activity.")
        }
    }
}