package com.srijanranger.quizapp

data class Question(
    val id: Int,
    val statement: String,
    val options: List<QuestionOption>,
    val isMultipleChoice: Boolean
)
