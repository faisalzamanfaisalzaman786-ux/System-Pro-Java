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
        
        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                           R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        
        for (int id : numberIds) {
            findViewById(id).setOnClickListener(v -> {
                Button b = (Button) v;
                if (isNewInput) { currentInput = ""; isNewInput = false; }
                currentInput += b.getText().toString();
                displayTextView.setText(currentInput);
            });
        }
        
        findViewById(R.id.btnAdd).setOnClickListener(v -> { if (!currentInput.isEmpty()) { firstNumber = Double.parseDouble(currentInput); operator = "+"; isNewInput = true; } });
        findViewById(R.id.btnSubtract).setOnClickListener(v -> { if (!currentInput.isEmpty()) { firstNumber = Double.parseDouble(currentInput); operator = "-"; isNewInput = true; } });
        findViewById(R.id.btnMultiply).setOnClickListener(v -> { if (!currentInput.isEmpty()) { firstNumber = Double.parseDouble(currentInput); operator = "×"; isNewInput = true; } });
        findViewById(R.id.btnDivide).setOnClickListener(v -> { if (!currentInput.isEmpty()) { firstNumber = Double.parseDouble(currentInput); operator = "÷"; isNewInput = true; } });
        
        findViewById(R.id.btnEquals).setOnClickListener(v -> {
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
        });
        
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput = ""; operator = ""; firstNumber = 0; isNewInput = true;
            displayTextView.setText("0");
        });
    }
}