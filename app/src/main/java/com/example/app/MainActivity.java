package com.calculator.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextView displayTextView;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean isNewInput = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        displayTextView = findViewById(R.id.displayTextView);
        
        // Number buttons
        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                           R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        
        for (int id : numberIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                Button b = (Button) v;
                appendNumber(b.getText().toString());
            });
        }
        
        // Operator buttons
        findViewById(R.id.btnAdd).setOnClickListener(v -> setOperator("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> setOperator("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> setOperator("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> setOperator("÷"));
        
        // Equals button
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculateResult());
        
        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());
    }
    
    private void appendNumber(String number) {
        if (isNewInput) {
            currentInput = "";
            isNewInput = false;
        }
        currentInput += number;
        displayTextView.setText(currentInput);
    }
    
    private void setOperator(String op) {
        if (!currentInput.isEmpty()) {
            firstNumber = Double.parseDouble(currentInput);
            operator = op;
            isNewInput = true;
        }
    }
    
    private void calculateResult() {
        if (!currentInput.isEmpty() && !operator.isEmpty()) {
            double secondNumber = Double.parseDouble(currentInput);
            double result = 0;
            
            switch (operator) {
                case "+": result = firstNumber + secondNumber; break;
                case "-": result = firstNumber - secondNumber; break;
                case "×": result = firstNumber * secondNumber; break;
                case "÷": result = secondNumber != 0 ? firstNumber / secondNumber : 0; break;
            }
            
            displayTextView.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
            operator = "";
            isNewInput = true;
        }
    }
    
    private void clearAll() {
        currentInput = "";
        operator = "";
        firstNumber = 0;
        isNewInput = true;
        displayTextView.setText("0");
    }
}