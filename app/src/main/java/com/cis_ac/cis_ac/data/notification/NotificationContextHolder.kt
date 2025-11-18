package com.cis_ac.cis_ac.data.notification

import android.app.Application
import android.content.Context


object NotificationContextHolder {
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun getContext(): Context? = applicationContext
}
