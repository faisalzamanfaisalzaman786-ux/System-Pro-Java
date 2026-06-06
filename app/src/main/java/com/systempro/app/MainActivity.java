package com.example.simpleapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextView textView;
    private Button clickButton;
    private int counter = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        textView = findViewById(R.id.textView);
        clickButton = findViewById(R.id.clickButton);
        
        // Set click listener on button
        clickButton.setOnClickListener(view -> {
            counter++;
            textView.setText("Button clicked: " + counter + " times");
            Toast.makeText(MainActivity.this, "Clicked " + counter, Toast.LENGTH_SHORT).show();
        });
        
        // Set initial text
        textView.setText("Welcome to Simple App!");
    }
}