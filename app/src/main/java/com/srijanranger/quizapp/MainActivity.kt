package com.srijanranger.quizapp

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private var btnStart: Button? = null
    private var etName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        etName = findViewById(R.id.etName)

        btnStart?.setOnClickListener {
            if (etName!!.text.isNotEmpty()) {
                Log.d(tag, "switching to quiz question screen")
                val intent = Intent(this, QuizQuestionActivity::class.java)
                intent.putExtra(Constant.KEY_USER_NAME, etName!!.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Oops! you haven't provided your name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is AppCompatEditText) {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}