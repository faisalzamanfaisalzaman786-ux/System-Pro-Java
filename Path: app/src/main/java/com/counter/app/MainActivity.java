package com.counter.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private Button minus, reset, plus;
    private int count = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tv = findViewById(R.id.tv);
        minus = findViewById(R.id.minus);
        reset = findViewById(R.id.reset);
        plus = findViewById(R.id.plus);
        
        tv.setText("0");
        
        minus.setOnClickListener(v -> { count--; tv.setText(String.valueOf(count)); });
        reset.setOnClickListener(v -> { count = 0; tv.setText("0"); });
        plus.setOnClickListener(v -> { count++; tv.setText(String.valueOf(count)); });
    }
}
