package com.faisal.systempro;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

// یہ امپورٹ شامل کرنا ضروری ہے تاکہ CameraX کا ایرر ختم ہو جائے
import androidx.camera.core.CameraSelector;

public class MainActivity extends AppCompatActivity {

    private EditText codeInput;
    private MaterialButton btnBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.id.activity_main); // یقینی بنائیں کہ یہ R.layout.activity_main ہی ہے

        codeInput = findViewById(R.id.code_input);
        btnBuild = findViewById(R.id.btn_build);

        btnBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeInput.getText().toString();
                if (!code.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Building Project...", Toast.LENGTH_SHORT).show();
                    
                    // یہاں آپ کیمرہ سلیکٹر کا استعمال کر سکتے ہیں اگر ضرورت ہو
                    // CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    
                } else {
                    Toast.makeText(MainActivity.this, "Please enter some code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
