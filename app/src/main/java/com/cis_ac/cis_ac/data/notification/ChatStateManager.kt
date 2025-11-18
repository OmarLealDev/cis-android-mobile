package com.cis_ac.cis_ac.data.notification


object ChatStateManager {

    private var currentChatId: String? = null
    private var isAppInForeground = true
    private var isChatScreenActive = false


    fun enterChat(chatId: String) {
        currentChatId = chatId
        isChatScreenActive = true
    }


    fun exitChat() {
        currentChatId = null
        isChatScreenActive = false
    }


    fun setAppForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }


    fun setChatScreenActive(active: Boolean) {
        isChatScreenActive = active
    }


    fun shouldShowNotification(chatId: String): Boolean {

        return !isAppInForeground || !isChatScreenActive || currentChatId != chatId
    }

}
