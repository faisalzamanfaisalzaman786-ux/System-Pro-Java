package com.system.pro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private String currentInput = "";
    private String lastOperator = "";
    private double firstValue = Double.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        // تمام بٹنز کے ID کے مطابق Listeners سیٹ کریں
        int[] buttonIds = {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_add, R.id.btn_sub, R.id.btn_mult, R.id.btn_div,
            R.id.btn_clear, R.id.btn_del, R.id.btn_equal
        };

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String text = b.getText().toString();

                if ("0123456789".contains(text)) {
                    currentInput += text;
                    display.setText(currentInput);
                } else if (text.equals("C")) {
                    currentInput = "";
                    firstValue = Double.NaN;
                    display.setText("0");
                } else if (text.equals("DEL")) {
                    if (currentInput.length() > 0) {
                        currentInput = currentInput.substring(0, currentInput.length() - 1);
                        display.setText(currentInput.isEmpty() ? "0" : currentInput);
                    }
                } else if (text.equals("=")) {
                    calculate();
                    lastOperator = "";
                } else {
                    if (!currentInput.isEmpty()) {
                        calculate();
                        lastOperator = text;
                        currentInput = "";
                    }
                }
            }
        };

        for (int id : buttonIds) {
            View btn = findViewById(id);
            if (btn != null) btn.setOnClickListener(listener);
        }
    }

    private void calculate() {
        if (!Double.isNaN(firstValue)) {
            if (!currentInput.isEmpty()) {
                double secondValue = Double.parseDouble(currentInput);
                switch (lastOperator) {
                    case "+": firstValue += secondValue; break;
                    case "-": firstValue -= secondValue; break;
                    case "*": firstValue *= secondValue; break;
                    case "/": 
                        if (secondValue != 0) firstValue /= secondValue;
                        else display.setText("Error");
                        break;
                }
                display.setText(String.valueOf(firstValue));
            }
        } else {
            try {
                firstValue = Double.parseDouble(currentInput);
            } catch (Exception e) { }
        }
    }
}
