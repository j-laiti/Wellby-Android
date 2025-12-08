package com.rcsi.wellby.chatTab.aiChat

import java.util.UUID

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val received: Boolean
) {
    companion object {
        fun fromChatGPTMessage(chatGPTMessage: ChatGPTMessage): AiMessage {
            return AiMessage(
                id = UUID.randomUUID().toString(),
                text = chatGPTMessage.content.firstOrNull()?.text?.value ?: "",
                received = chatGPTMessage.role == "assistant"
            )
        }
    }
}