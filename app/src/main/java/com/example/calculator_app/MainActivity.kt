package com.example.calculator_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var resultViewer: TextView
    private lateinit var operationsViewer: TextView

    private var currentInput = ""
    private var currentOperator = ""
    private var expression = ""
    private var currentResult = 0.0

    private var isNewInput = true
    private var isResultDisplayed = false

    private fun calc(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else 0.0
            else -> b
        }
    }

    private fun updateOperationHistory() {
        operationsViewer.text = expression

        val scrollView: ScrollView = findViewById(R.id.scrollViewOperations)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultViewer = findViewById(R.id.resultViewer)
        operationsViewer = findViewById(R.id.operationsViewer)

        val prefs = getSharedPreferences("calculatorPrefs", MODE_PRIVATE)
        expression = prefs.getString("expression", "") ?: ""
        currentInput = prefs.getString("currentInput", "") ?: ""
        currentResult = prefs.getString("currentResult", "0")?.toDouble() ?: 0.0
        currentOperator = prefs.getString("currentOperator", "") ?: ""

        resultViewer.text = if (currentInput.isNotEmpty()) currentInput else currentResult.toString()
        updateOperationHistory()
    }

    override fun onPause() {
        super.onPause()
        val prefs = getSharedPreferences("calculatorPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("expression", expression)
        editor.putString("currentInput", currentInput)
        editor.putString("currentResult", currentResult.toString())
        editor.putString("currentOperator", currentOperator)
        editor.apply()
    }

    fun onDigit(view: View) {
        val button = view as Button

        if (isResultDisplayed) {
            currentInput = ""
            currentOperator = ""
            currentResult = 0.0
            isResultDisplayed = false
            isNewInput = true
        }

        if (isNewInput) {
            currentInput = ""
            isNewInput = false
        }

        currentInput += button.text
        resultViewer.text = currentInput
    }

    fun onDecimal(view: View) {
        if (!currentInput.contains(".")) {
            currentInput += "."
            resultViewer.text = currentInput
        }
    }

    fun onOperator(view: View) {
        val button = view as Button
        val op = button.text.toString()

        val value = currentInput.toDoubleOrNull() ?: return

        if (isResultDisplayed) {
            currentOperator = ""
            isResultDisplayed = false
        }

        if (currentOperator.isEmpty()) {
            currentResult = value
            expression += value.toString()
        } else {
            currentResult = calc(currentResult, value, currentOperator)
            expression += " $currentOperator $value"
        }

        currentOperator = op
        isNewInput = true

        resultViewer.text = currentResult.toString()
        updateOperationHistory()
    }

    fun onBackspace(view: View) {
        if (isNewInput) return

        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            resultViewer.text = if (currentInput.isEmpty()) "0" else currentInput
        }
    }

    fun onEqual(view: View) {
        val value = currentInput.toDoubleOrNull() ?: return

        currentResult = calc(currentResult, value, currentOperator)
        expression += " $currentOperator $value = $currentResult\n"

        resultViewer.text = currentResult.toString()

        currentOperator = ""
        currentInput = currentResult.toString()
        isResultDisplayed = true
        isNewInput = true
        updateOperationHistory()
    }

    fun onClear(view: View) {
        currentInput = ""
        currentResult = 0.0
        currentOperator = ""
        expression = ""
        isNewInput = true
        isResultDisplayed = false

        resultViewer.text = "0"
        updateOperationHistory()

        val prefs = getSharedPreferences("calculatorPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
