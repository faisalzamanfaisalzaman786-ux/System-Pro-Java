package com.systempro.javaui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {
    private TextView display;
    private String currentInput = "";
    private String operator = "";
    private double firstValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.textView);
    }

    public void onNumberClick(View view) {
        Button btn = (Button) view;
        currentInput += btn.getText().toString();
        display.setText(currentInput);
    }

    public void onOperatorClick(View view) {
        Button btn = (Button) view;
        operator = btn.getText().toString();
        firstValue = Double.parseDouble(currentInput);
        currentInput = "";
    }

    public void onEqualClick(View view) {
        double secondValue = Double.parseDouble(currentInput);
        double result = 0;
        switch (operator) {
            case "+": result = firstValue + secondValue; break;
            case "-": result = firstValue - secondValue; break;
            case "*": result = firstValue * secondValue; break;
            case "/": result = firstValue / secondValue; break;
        }
        display.setText(String.valueOf(result));
        currentInput = String.valueOf(result);
    }

    public void onClearClick(View view) {
        currentInput = "";
        operator = "";
        firstValue = 0;
        display.setText("0");
    }
}