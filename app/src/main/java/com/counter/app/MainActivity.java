package com.counter.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextView counterTextView;
    private Button incrementButton;
    private Button decrementButton;
    private Button resetButton;
    private int counter = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        counterTextView = findViewById(R.id.counterTextView);
        incrementButton = findViewById(R.id.incrementButton);
        decrementButton = findViewById(R.id.decrementButton);
        resetButton = findViewById(R.id.resetButton);
        
        // Set initial value
        updateCounterDisplay();
        
        // Increment button click
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                updateCounterDisplay();
            }
        });
        
        // Decrement button click
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter--;
                updateCounterDisplay();
            }
        });
        
        // Reset button click
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter = 0;
                updateCounterDisplay();
            }
        });
    }
    
    private void updateCounterDisplay() {
        counterTextView.setText(String.valueOf(counter));
        
        // Optional: Change color based on value
        if (counter > 0) {
            counterTextView.setTextColor(getColor(android.R.color.holo_green_dark));
        } else if (counter < 0) {
            counterTextView.setTextColor(getColor(android.R.color.holo_red_dark));
        } else {
            counterTextView.setTextColor(getColor(android.R.color.black));
        }
    }
}
