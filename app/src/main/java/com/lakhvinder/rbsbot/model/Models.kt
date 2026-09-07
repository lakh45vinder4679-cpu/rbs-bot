package com.lakhvinder.rbsbot.model

data class Mcq(
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String = ""
)

data class ChatTurn(val role: String, val text: String)
