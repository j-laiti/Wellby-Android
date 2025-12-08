package com.rcsi.wellby.chatTab.aiChat

data class ChatGPTAssistant(
    val id: String,
    val name: String
)

data class ChatGPTThread(
    val id: String
)

data class ChatGPTRun(
    val id: String,
    val status: String
)

data class ChatGPTRequest(
    val role: String,
    val content: String
)

data class ChatGPTResponse(
    val data: List<ChatGPTMessage>
)

data class ChatGPTMessage(
    val role: String,
    val content: List<ChatGPTContent>
)

data class ChatGPTContent(
    val type: String,
    val text: ChatGPTText
)

data class ChatGPTText(
    val value: String,
    val annotations: List<String>
)
