package com.counter.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView counterText;
    private Button incrementBtn;
    private Button decrementBtn;
    private Button resetBtn;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counterText = findViewById(R.id.counterText);
        incrementBtn = findViewById(R.id.incrementBtn);
        decrementBtn = findViewById(R.id.decrementBtn);
        resetBtn = findViewById(R.id.resetBtn);

        counterText.setText("0");

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                counterText.setText(String.valueOf(count));
            }
        });

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                counterText.setText(String.valueOf(count));
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                counterText.setText("0");
            }
        });
    }
}