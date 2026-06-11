package com.calculator.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView displayTextView;
    private String currentInput = "";
    private double firstNumber = 0;
    private String operator = "";
    private boolean isNewInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayTextView = findViewById(R.id.displayTextView);

        int[] numberIds = {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (isNewInput) {
                    currentInput = b.getText().toString();
                    isNewInput = false;
                } else {
                    currentInput += b.getText().toString();
                }
                displayTextView.setText(currentInput);
            }
        };

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setOperator("+"); }
        });

        findViewById(R.id.btnSubtract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setOperator("-"); }
        });

        findViewById(R.id.btnMultiply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setOperator("×"); }
        });

        findViewById(R.id.btnDivide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setOperator("÷"); }
        });

        findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { calculateResult(); }
        });

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { clearDisplay(); }
        });
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
                case "÷": 
                    if (secondNumber != 0) result = firstNumber / secondNumber;
                    break;
            }

            displayTextView.setText(String.valueOf(result));
            currentInput = String.valueOf(result);
            operator = "";
            isNewInput = true;
        }
    }

    private void clearDisplay() {
        currentInput = "";
        firstNumber = 0;
        operator = "";
        isNewInput = true;
        displayTextView.setText("0");
    }
}