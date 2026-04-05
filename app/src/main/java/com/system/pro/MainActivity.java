package com.system.pro; // آپ کے فولڈر اسٹرکچر (com.system.pro) کے مطابق اپڈیٹ کر دیا گیا ہے

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

// CameraX Import (بلڈ ایرر ختم کرنے کے لیے)
import androidx.camera.core.CameraSelector;

public class MainActivity extends AppCompatActivity {

    private EditText codeInput;
    private MaterialButton btnBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // یہاں layout کا نام وہی رکھیں جو آپ کی xml فائل کا ہے
        setContentView(R.layout.activity_main);

        codeInput = findViewById(R.id.code_input);
        btnBuild = findViewById(R.id.btn_build);

        btnBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeInput.getText().toString();
                if (!code.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Building Project...", Toast.LENGTH_SHORT).show();
                    
                    // CameraSelector کا استعمال تاکہ کمپائلر اسے پہچان لے
                    CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
                } else {
                    Toast.makeText(MainActivity.this, "Please enter some code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
