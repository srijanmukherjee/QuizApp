package com.srijanranger.quizapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class QuizResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        val tvResult: TextView = findViewById(R.id.tvResult)

        val name = intent.getStringExtra(Constant.KEY_USER_NAME)
        val totalQuestions = intent.getIntExtra(Constant.KEY_TOTAL_QUESTIONS, 0)
        val correctAnswers = intent.getIntExtra(Constant.KEY_CORRECT_ANSWERS, 0)

        tvResult.text = getString(R.string.resultMsg, name, correctAnswers, totalQuestions)
    }
}