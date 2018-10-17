package me.gr.jobscheduler

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Messenger
import android.os.PersistableBundle
import kotlinx.android.synthetic.main.activity_main.*
import me.gr.jobscheduler.common.MESSENGER_INTENT_KEY
import me.gr.jobscheduler.common.WORK_DURATION_KEY
import me.gr.jobscheduler.service.TaskService
import me.gr.jobscheduler.util.TaskHandler
import org.jetbrains.anko.*

class MainActivity : Activity(), AnkoLogger {
    private val taskHandler by lazy(LazyThreadSafetyMode.NONE) {
        TaskHandler(this)
    }
    private val taskService by lazy(LazyThreadSafetyMode.NONE) {
        ComponentName(this, TaskService::class.java)
    }
    private val jobScheduler by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }
    private var jobId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        schedule_button.setOnClickListener { scheduleJob() }
        cancel_button.setOnClickListener { cancelAllJobs() }
        finish_task.setOnClickListener { finishJob() }
    }

    override fun onStart() {
        super.onStart()
        startService<TaskService>(MESSENGER_INTENT_KEY to Messenger(taskHandler))
    }

    override fun onStop() {
        stopService<TaskService>()
        super.onStop()
    }

    private fun scheduleJob() {
        val builder = JobInfo.Builder(jobId++, taskService)

        val delay = delay_edit.text.toString()
        if (delay.isNotEmpty()) {
            builder.setMinimumLatency(delay.toLong() * 1000)
        }

        val deadline = deadline_edit.text.toString()
        if (deadline.isNotEmpty()) {
            builder.setOverrideDeadline(deadline.toLong() * 1000)
        }

        when {
            any_radio.isChecked -> {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            }
            wifi_radio.isChecked -> {
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            }
        }

        val extras = PersistableBundle()
        var duration = duration_edit.text.toString()
        if (duration.isEmpty()) duration = "1"
        extras.putLong(WORK_DURATION_KEY, duration.toLong() * 1000)

        builder.setRequiresDeviceIdle(idle_check.isChecked)
            .setRequiresCharging(charging_check.isChecked)
            .setExtras(extras)

        debug("Scheduling job")
        jobScheduler.schedule(builder.build())
    }

    private fun cancelAllJobs() {
        jobScheduler.cancelAll()
        toast(R.string.all_jobs_cancelled)
    }

    private fun finishJob() {
        val allPendingJobs = jobScheduler.allPendingJobs
        if (allPendingJobs.isNotEmpty()) {
            val id = allPendingJobs.first().id
            jobScheduler.cancel(id)
            toast(getString(R.string.cancelled_job, id))
        } else {
            toast(getString(R.string.no_jobs_to_cancel))
        }
    }
}
