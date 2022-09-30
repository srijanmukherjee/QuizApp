package com.srijanranger.quizapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

// TODO Too much code, needs some serious refactoring
class QuizQuestionActivity : AppCompatActivity(), View.OnClickListener {
    private val tag = "QuizQuestionActivity"
    private var progressBarLayout: LinearLayout? = null
    private var contentLayout: LinearLayout? = null
    private var tvQuestion: TextView? = null
    private var tvQuestionType: TextView? = null
    private val tvOptions: MutableList<TextView> = ArrayList()
    private var progressBar: ProgressBar? = null
    private var tvProgressText: TextView? = null
    private var btnSubmit: Button? = null
    private val mQuestions: MutableList<Question> = ArrayList()
    private var mCurrentQuestion = 0
    private var mSelectedOptions: ArrayList<Boolean> = arrayListOf(false, false, false, false, false, false)
    private var canSelectOption = true

    private var mScore: Double = 0.0
    private var mCorrectAnswers: Int = 0
    private var mUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_question)

        progressBarLayout   = findViewById(R.id.progressBarLayout)
        contentLayout       = findViewById(R.id.contentLayout)
        tvQuestion          = findViewById(R.id.tvQuestion)
        tvQuestionType      = findViewById(R.id.tvQuestionType)
        progressBar         = findViewById(R.id.progressBar)
        tvProgressText      = findViewById(R.id.tvProgressText)
        btnSubmit           = findViewById(R.id.btnSubmit)

        tvOptions.add(findViewById(R.id.tvOptionOne))
        tvOptions.add(findViewById(R.id.tvOptionTwo))
        tvOptions.add(findViewById(R.id.tvOptionThree))
        tvOptions.add(findViewById(R.id.tvOptionFour))
        tvOptions.add(findViewById(R.id.tvOptionFive))
        tvOptions.add(findViewById(R.id.tvOptionSix))

        for (tv in tvOptions) tv.setOnClickListener(this)
        btnSubmit?.setOnClickListener(this)

        val model: QuestionModel = ViewModelProvider(this)[QuestionModel::class.java]
        model.getQuestions().observe(this) { questions ->
            onQuestionLoad(questions)
        }

        mUser = intent.getStringExtra(Constant.KEY_USER_NAME).toString()
    }

    private fun selectMultipleOptions(tv: TextView, idx: Int) {
        if (mSelectedOptions[idx]) {
            tv.setTextColor(Color.parseColor("#7A8089"))
            tv.typeface = Typeface.DEFAULT
            tv.background = ContextCompat.getDrawable(this@QuizQuestionActivity, R.drawable.default_option_border_bg)
            mSelectedOptions[idx] = false
        } else {
            tv.setTextColor(ContextCompat.getColor(this@QuizQuestionActivity, R.color.purple_500))
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            tv.background = ContextCompat.getDrawable(this@QuizQuestionActivity, R.drawable.selected_option_border_bg)
            mSelectedOptions[idx] = true
        }
    }

    private fun selectSingleOption(tv: TextView, idx: Int) {
        if (mSelectedOptions[idx]) return

        for (i in tvOptions.indices) {
            if (i == idx) continue
            mSelectedOptions[i] = false
            tvOptions[i].setTextColor(Color.parseColor("#7A8089"))
            tvOptions[i].typeface = Typeface.DEFAULT
            tvOptions[i].background = ContextCompat.getDrawable(this@QuizQuestionActivity, R.drawable.default_option_border_bg)
        }

        tv.setTextColor(ContextCompat.getColor(this@QuizQuestionActivity, R.color.purple_500))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(this@QuizQuestionActivity, R.drawable.selected_option_border_bg)
        mSelectedOptions[idx] = true
    }

    private fun selectOption(idx: Int) {
        if (!canSelectOption) return
        Log.d(tag, "selectOption(idx = $idx)")
        val tv: TextView = tvOptions[idx]
        val question: Question = mQuestions[mCurrentQuestion - 1]

        if (question.isMultipleChoice) selectMultipleOptions(tv, idx)
        else selectSingleOption(tv, idx)

        updateSubmitBtn()
    }

    private fun onQuestionLoad(questions: List<Question>) {
        if (questions.isEmpty()) {
            Log.d(tag, "Questions couldn't be loaded")
            val intent = Intent(this, NetworkErrorActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        progressBarLayout!!.visibility = View.GONE
        contentLayout!!.visibility = View.VISIBLE
        Log.d(tag, "Questions loaded")

        mQuestions.addAll(questions)
        progressBar!!.max = mQuestions.size
        nextQuestion()
    }

    private fun resetSelection() {
        for (i in mSelectedOptions.indices) {
            mSelectedOptions[i] = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun nextQuestion() {
        mCurrentQuestion++

        val question = mQuestions[mCurrentQuestion - 1]
        resetSelection()
        tvQuestion!!.text = question.statement

        for (i in question.options.indices) {
            tvOptions[i].visibility = View.VISIBLE
            tvOptions[i].text = question.options[i].statement
            tvOptions[i].background = ContextCompat.getDrawable(this@QuizQuestionActivity, R.drawable.default_option_border_bg)
            tvOptions[i].setTextColor(Color.parseColor("#7A8089"))
            tvOptions[i].typeface = Typeface.DEFAULT
        }

        // hide the other options
        for (i in question.options.size until tvOptions.size) {
            tvOptions[i].visibility = View.GONE
        }

        tvQuestionType!!.text = if (question.isMultipleChoice) "Multiple Choice" else "Single Choice"
        progressBar!!.progress = mCurrentQuestion
        tvProgressText!!.text = "$mCurrentQuestion/${mQuestions.size}"

        updateSubmitBtn()
    }

    private fun enableSubmitBtn() {
        btnSubmit!!.backgroundTintList = ContextCompat.getColorStateList(this@QuizQuestionActivity, com.google.android.material.R.color.design_default_color_primary)
        btnSubmit!!.setTextColor(ContextCompat.getColor(this@QuizQuestionActivity, R.color.white))
        btnSubmit!!.isEnabled = true
    }

    private fun disableSubmitBtn() {
        btnSubmit!!.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#eeeeee"))
        btnSubmit!!.setTextColor(Color.parseColor("#7A8089"))
        btnSubmit!!.isEnabled = false
    }

    private fun updateSubmitBtn() {
        if (countOptionsSelected() > 0) enableSubmitBtn()
        else disableSubmitBtn()
    }

    private fun countOptionsSelected(): Int {
        var count = 0

        for (i in mSelectedOptions) {
            if (i) count++
        }

        return count
    }

    private fun applyAnswer(tv: TextView, style: Int) {
        if (style != R.drawable.correct_not_selected_option_border_bg)
            tv.setTextColor(ContextCompat.getColor(this@QuizQuestionActivity, R.color.white))
        tv.background = ContextCompat.getDrawable(this@QuizQuestionActivity, style)
    }

    private fun revealAnswers() {
        val question = mQuestions[mCurrentQuestion - 1]
        var totalCorrectAnswers = 0
        var selectedCorrectAnswers = 0

        for (i in question.options.indices) {
            if (question.options[i].isCorrect) totalCorrectAnswers += 1

            if (mSelectedOptions[i]) {
                val style = if (question.options[i].isCorrect) R.drawable.correct_option_border_bg else R.drawable.incorrect_option_border_bg
                if (question.options[i].isCorrect) {
                    selectedCorrectAnswers += 1
                }
                applyAnswer(tvOptions[i], style)
            } else if (question.options[i].isCorrect) {
                applyAnswer(tvOptions[i], R.drawable.correct_not_selected_option_border_bg)
            }
        }

        mScore += selectedCorrectAnswers / totalCorrectAnswers.toDouble()

        if (totalCorrectAnswers == selectedCorrectAnswers)
            mCorrectAnswers++

        resetSelection()
    }

    private fun showResult() {
        val intent = Intent(this, QuizResultActivity::class.java)
        intent.putExtra(Constant.KEY_SCORE, mScore)
        intent.putExtra(Constant.KEY_TOTAL_QUESTIONS, mQuestions.size)
        intent.putExtra(Constant.KEY_CORRECT_ANSWERS, mCorrectAnswers)
        intent.putExtra(Constant.KEY_USER_NAME, mUser)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun onSubmit() {
        if (countOptionsSelected() == 0) {
            canSelectOption = true
            if (mCurrentQuestion >= mQuestions.size) {
                showResult()
            } else {
                btnSubmit!!.text = "SUBMIT"
                nextQuestion()
            }
        } else {
            revealAnswers()
            btnSubmit!!.text = if (mCurrentQuestion >= mQuestions.size) "FINISH" else "GO TO NEXT QUESTION"
            canSelectOption = false
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.tvOptionOne -> selectOption(0)
            R.id.tvOptionTwo -> selectOption(1)
            R.id.tvOptionThree -> selectOption(2)
            R.id.tvOptionFour -> selectOption(3)
            R.id.tvOptionFive -> selectOption(4)
            R.id.tvOptionSix -> selectOption(5)
            R.id.btnSubmit -> onSubmit()
        }
    }
}